package tracking

import org.opencv.core.*
import tracking.RectTracker
import types.AggregatedDetections
import utils.zip

class AggregatedDigitDetectionTracker {
    private val tracker = RectTracker()

    fun track(
        prevFrameGray: Mat,
        nextFrameGray: Mat,
        prevObjects: List<AggregatedDetections>
    ): List<AggregatedDetections> {
        val prevBoxes = prevObjects.map { it.box }
        val (nextBoxes, statuses) = tracker.track(prevFrameGray, nextFrameGray, prevBoxes)

        val nextObjects = mutableListOf<AggregatedDetections>()
        for ((prevObject, nextBox, status) in prevObjects.zip(nextBoxes, statuses)) {
            if (!status || isAbnormalTrack(prevObject.box, nextBox)) {
                continue
            }
            nextObjects.add(AggregatedDetections(nextBox, prevObject.score, prevObject.digitsCounts))
        }

        return nextObjects
    }

    private fun isAbnormalTrack(prevBox: Rect, nextBox: Rect): Boolean {
        if (nextBox.tl().x > nextBox.br().x || nextBox.tl().y > nextBox.br().y) {
            return true
        }
        val wRatio = prevBox.width / nextBox.width.toDouble()
        val hRatio = prevBox.height / nextBox.height.toDouble()

        return isNormalRatio(wRatio).not() || isNormalRatio(hRatio).not()
    }

    private companion object {
        const val delta = .25
        inline fun isNormalRatio(ratio: Double) =
            1 + delta > ratio && ratio > 1 - delta
    }
}


