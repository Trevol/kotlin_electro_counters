import org.opencv.core.Rect
import types.AggregatedDetections

class AggregatingBoxGroupingDigitExtractor {
    fun extract(currentDetections: Collection<DigitDetectionResult>, prevDetections: Collection<AggregatedDetections>) {

    }
}

data class GroupBoxesResult(val groupIndices: Collection<Int>, val keptIndices: Collection<Int>)

fun groupBoxes(boxes: Collection<Rect>, scores: Collection<Float>?, overlap_threshold: Float): GroupBoxesResult {
    assert(scores == null || (boxes.size == scores.size))
    assert(overlap_threshold >= 0)

    //sort indices by score desc
    for (index in indicesByScore(boxes, scores)) {
        throw NotImplementedError()
    }

    val groupIndices: MutableCollection<Int> = mutableListOf()
    val keptIndices: MutableCollection<Int> = mutableListOf()

    if (scores == null) {
        throw NotImplementedError()
    }

    return GroupBoxesResult(groupIndices, keptIndices)


}

data class KeepAndMaxOverlapIndex(val keep: Boolean, val maxOverlapIndex: Int)

private fun shouldKeepBox(
    currentBoxIndex: Int,
    boxes: Collection<Rect>,
    keptIndices: Collection<Int>,
    overlap_threshold: Float
): KeepAndMaxOverlapIndex {
    throw NotImplementedError()
}


private inline fun indicesByScore(boxes: Collection<Rect>, scores: Collection<Float>?): List<Int> {
    if (scores == null) {
        return boxes.mapIndexed { index, _ -> index }
    }
    return scores.mapIndexed { index, score -> index to score }
        .sortedByDescending { it.second }
        .map { it.first }
}