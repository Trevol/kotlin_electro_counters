package tracking

import org.opencv.core.*
import org.opencv.utils.Converters
import org.opencv.video.SparsePyrLKOpticalFlow
import utils.println

class RectTracker {
    data class Result(val nextBoxes: List<Rect>, val statuses: List<Boolean>)

    fun track(prevImg: Mat, nextImg: Mat, prevBoxes: List<Rect>): Result {
        if (prevBoxes.isEmpty()) {
            return Result(listOf(), listOf())
        }
        val prevPts = prevBoxes.toTrackedPts()
        val (nextPts, nextPtsStatuses) = trackPoints(prevImg, nextImg, prevPts)
        println("prevPts", prevPts)
        println("nextPts", nextPts)
        println("----------------------")

        val nextPtsIter = nextPts.iterator()
        val statusesIter = nextPtsStatuses.iterator()

        val nextBoxes = mutableListOf<Rect>()
        val statuses = mutableListOf<Boolean>()
        while (nextPtsIter.hasNext()) {
            val point1 = nextPtsIter.next()
            val point2 = nextPtsIter.next()
            val status1 = statusesIter.next()
            val status2 = statusesIter.next()

            nextBoxes.add(Rect(point1, point2))
            statuses.add(status1 == statusOk && status2 == statusOk)
        }

        return Result(nextBoxes, statuses)
    }

    fun trackPoints(prevImg: Mat, nextImg: Mat, prevPts: List<Point>): Pair<List<Point>, List<Byte>> {
        val matOfNextPts = MatOfPoint2f()
        val matOfStatuses = MatOfByte()
        val matOfPrevPts = Converters.vector_Point2f_to_Mat(prevPts)
        optflow.calc(prevImg, nextImg, matOfPrevPts, matOfNextPts, matOfStatuses)
        val nextPts = mutableListOf<Point>().apply { Converters.Mat_to_vector_Point2f(matOfNextPts, this) }
        val statuses = mutableListOf<Byte>().apply { Converters.Mat_to_vector_uchar(matOfStatuses, this) }
        return nextPts to statuses
    }

    private companion object {
        private val optflow = SparsePyrLKOpticalFlow.create()

        val statusOk: Byte = 1

        fun List<Rect>.toTrackedPts(): List<Point> {
            val pts = mutableListOf<Point>()
            for (box in this) {
                pts.add(box.tl())
                pts.add(box.br())
            }
            return pts
        }
    }
}
