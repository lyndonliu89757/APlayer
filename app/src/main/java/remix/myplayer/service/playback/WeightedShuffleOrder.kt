package remix.myplayer.service.playback

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

/**
 * 加权随机播放顺序实现
 * 使播放列表中靠前的歌曲有更高的播放概率
 */
@UnstableApi
class WeightedShuffleOrder(
  length: Int,
  private val seed: Long = System.currentTimeMillis(),
  private val weightConfig: WeightConfig = WeightConfig()
) : ShuffleOrder {

  data class WeightConfig(
    val strategy: WeightStrategy = WeightStrategy.EXPONENTIAL,
    val decayFactor: Float = 1.5f,
    val frontMultiplier: Float = 3.0f,
    val backMultiplier: Float = 0.5f
  )

  enum class WeightStrategy {
    LINEAR,           // 线性权重
    EXPONENTIAL,      // 指数权重
    QUADRATIC,        // 二次权重
    HYBRID            // 混合权重
  }

  private val random = Random(seed)
  private val shuffledIndices: IntArray
  private val indexInShuffled: IntArray

  init {
    require(length >= 0) { "length must be non-negative" }
    shuffledIndices = generateWeightedShuffle(length, weightConfig)
    indexInShuffled = IntArray(length)
    for (i in 0 until length) {
      indexInShuffled[shuffledIndices[i]] = i
    }
  }

  private fun generateWeightedShuffle(length: Int, config: WeightConfig): IntArray {
    if (length == 0) return IntArray(0)

    val result = IntArray(length)
    val available = mutableListOf<Int>()

    // 初始化可用索引
    for (i in 0 until length) {
      available.add(i)
    }

    for (i in 0 until length) {
      // 计算每个可用索引的权重
      val weights = available.map { index ->
        calculateWeight(index, length, config)
      }

      // 加权随机选择
      val selectedIndex = weightedRandomSelect(available, weights)
      result[i] = selectedIndex
      available.remove(selectedIndex)
    }

    return result
  }

  private fun calculateWeight(index: Int, total: Int, config: WeightConfig): Float {
    if (total <= 1) return 1.0f

    val position = index.toFloat() / (total - 1)  // 归一化位置 (0-1)

    return when (config.strategy) {
      WeightStrategy.LINEAR -> {
        // 线性权重：前面的权重大
        1.0f - position
      }

      WeightStrategy.EXPONENTIAL -> {
        // 指数权重：exp(-decay * position)
        val weight = exp(-config.decayFactor * position)
        weight.toFloat()
      }

      WeightStrategy.QUADRATIC -> {
        // 二次权重：前面的权重衰减更快
        (1.0f - position).pow(2)
      }

      WeightStrategy.HYBRID -> {
        // 混合权重：前20%高权重，中间中等，后20%低权重
        when {
          position < 0.2f -> config.frontMultiplier
          position < 0.8f -> 1.0f
          else -> config.backMultiplier
        }
      }
    } + 0.1f  // 保证最低权重，防止除零
  }

  private fun weightedRandomSelect(items: List<Int>, weights: List<Float>): Int {
    val totalWeight = weights.sum()
    var randomValue = random.nextFloat() * totalWeight

    for (i in weights.indices) {
      randomValue -= weights[i]
      if (randomValue <= 0) {
        return items[i]
      }
    }
    return items.last()
  }

  override fun getLength(): Int = shuffledIndices.size

  override fun getFirstIndex(): Int = if (shuffledIndices.isNotEmpty()) shuffledIndices[0] else C.INDEX_UNSET

  override fun getLastIndex(): Int = if (shuffledIndices.isNotEmpty()) shuffledIndices.last() else C.INDEX_UNSET

  override fun getNextIndex(index: Int): Int {
    if (index < 0 || index >= shuffledIndices.size) return C.INDEX_UNSET
    val position = indexInShuffled[index]
    return if (position + 1 < shuffledIndices.size) shuffledIndices[position + 1] else C.INDEX_UNSET
  }

  override fun getPreviousIndex(index: Int): Int {
    if (index < 0 || index >= shuffledIndices.size) return C.INDEX_UNSET
    val position = indexInShuffled[index]
    return if (position - 1 >= 0) shuffledIndices[position - 1] else C.INDEX_UNSET
  }

  override fun cloneAndInsert(insertIndex: Int, insertCount: Int): ShuffleOrder {
    if (insertCount == 0) return this

    val newLength = shuffledIndices.size + insertCount

    // 创建新的加权随机顺序
    val newSeed = if (shuffledIndices.size > 0) {
      (seed + insertIndex + insertCount) % Long.MAX_VALUE
    } else {
      System.currentTimeMillis()
    }

    return WeightedShuffleOrder(newLength, newSeed, weightConfig)
  }

  override fun cloneAndRemove(removeFromIndex: Int, removeToIndex: Int): ShuffleOrder {
    val removeCount = removeToIndex - removeFromIndex
    if (removeCount <= 0) return this

    val newLength = shuffledIndices.size - removeCount
    if (newLength < 0) return cloneAndClear()

    val newSeed = (seed + removeFromIndex + removeToIndex) % Long.MAX_VALUE
    return WeightedShuffleOrder(newLength, newSeed, weightConfig)
  }

  override fun cloneAndClear(): ShuffleOrder {
    return WeightedShuffleOrder(0, 0, weightConfig)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as WeightedShuffleOrder

    if (!shuffledIndices.contentEquals(other.shuffledIndices)) return false
    if (weightConfig != other.weightConfig) return false
    if (seed != other.seed) return false

    return true
  }

  override fun hashCode(): Int {
    var result = shuffledIndices.contentHashCode()
    result = 31 * result + weightConfig.hashCode()
    result = 31 * result + seed.hashCode()
    return result
  }
}
