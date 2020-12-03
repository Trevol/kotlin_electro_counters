import org.opencv.core.Rect
import types.AggregatedDetections
import types.DigitAtBox
import types.DigitCount
import kotlin.math.max
import kotlin.math.min

data class Digits_AggregatedDetections(
    val digitsAtBoxes: List<DigitAtBox>,
    val aggregatedDetections: List<AggregatedDetections>
)

class AggregatingBoxGroupingDigitExtractor {
    fun extract(currentDetections: Collection<DigitDetectionResult>, prevDetections: Collection<AggregatedDetections>):
            Digits_AggregatedDetections {

        val boxes = currentDetections.map { it.boxInImage } +
                prevDetections.map { it.box }
        val scores = currentDetections.map { it.score } + prevDetections.map { it.score }
        val digitsCounts = currentDetections.map { listOf(DigitCount(it.digit, 1)) } +
                prevDetections.map { it.digitsCounts }

        val aggregatedDetections = groupBoxes(boxes, scores, .4f)
            .groupIndices.zip(digitsCounts)
            .groupBy({ it.first }, { it.second })
            .map { index, digitsCountsByBox ->
                AggregatedDetections(boxes[index], scores[index], merge(digitsCountsByBox))
            }

        val digits = aggregatedDetections.filter { it.totalCount() >= minBoxesInGroup }
            .map { DigitAtBox(it.digitWithMaxCount(), it.box) }

        return Digits_AggregatedDetections(digits, aggregatedDetections)
    }

    companion object {
        const val minBoxesInGroup = 3

        inline fun merge(digitsCountsByBox: List<List<DigitCount>>) =
            digitsCountsByBox.flatten()
                .groupBy({ it.digit }, { it.count })
                .map { digit, digitCounts -> DigitCount(digit, digitCounts.sum()) }


        inline fun AggregatedDetections.totalCount() = digitsCounts.sumOf { it.count }
        inline fun AggregatedDetections.digitWithMaxCount() = digitsCounts.maxByOrNull { it.count }!!.digit

        inline fun <K, V, R> Map<out K, V>.map(transform: (K, V) -> R) =
            this.map { entry -> transform(entry.key, entry.value) }
    }
}

data class GroupBoxesResult(val groupIndices: Collection<Int>, val keptIndices: Collection<Int>)

fun groupBoxes(boxes: List<Rect>, scores: Collection<Float>?, overlap_threshold: Float): GroupBoxesResult {
    assert(scores == null || (boxes.size == scores.size))
    assert(overlap_threshold >= 0)

    data class IndexToGroup(val index: Int, val groupIndex: Int)

    val indicesToGroups = mutableListOf<IndexToGroup>()
    val keptIndices = mutableListOf<Int>()
    for (index in indicesByScore(boxes, scores)) {
        val (keep, maxOverlapIndex) = shouldKeepBox(index, boxes, keptIndices, overlap_threshold)
        if (keep) {
            keptIndices.add(index)
            indicesToGroups.add(IndexToGroup(index, index))
        } else {
            indicesToGroups.add(IndexToGroup(index, maxOverlapIndex))
        }
    }
    // restore original boxes order (sort by box index) and select groupIndex
    val groupIndices = indicesToGroups.sortedBy { it.index }.map { it.groupIndex }
    return GroupBoxesResult(groupIndices, keptIndices)
}

private fun shouldKeepBox(
    currentBoxIndex: Int,
    boxes: List<Rect>,
    keptIndices: Collection<Int>,
    overlap_threshold: Float
): ShouldKeepBoxResult {
    val box = boxes[currentBoxIndex]
    var keep = true
    var maxOverlap = -1.0f
    var maxOverlapIndex = -1
    for (keptIndex in keptIndices) {
        val overlap = box.overlap(boxes[keptIndex])
        if (keep) {
            keep = overlap <= overlap_threshold
        }
        if (overlap > maxOverlap) {
            maxOverlap = overlap
            maxOverlapIndex = keptIndex
        }
    }
    return ShouldKeepBoxResult(keep, maxOverlapIndex)
}

data class ShouldKeepBoxResult(val keep: Boolean, val maxOverlapIndex: Int)

private inline fun indicesByScore(boxes: Collection<Rect>, scores: Collection<Float>?) =
    scores?.mapIndexed { index, score -> index to score }?.sortedByDescending { it.second }?.map { it.first }
        ?: boxes.mapIndexed { index, _ -> index }

fun Rect.overlap(other: Rect) = this.iou(other)

fun Rect.iou(other: Rect): Float {
    val intersectArea = this.intersection(other).area()
    return (intersectArea / (area() + other.area() - intersectArea)).toFloat()
}

fun Rect.intersection(other: Rect): Rect {
    val x1 = max(x, other.x)
    val y1 = max(y, other.y)
    val w = min(x + width, other.x + other.width) - x1
    val h = min(y + height, other.y + other.height) - y1
    if (w <= 0 || h <= 0)
        return Rect(0, 0, 0, 0)
    return Rect(x1, y1, w, h)
}
