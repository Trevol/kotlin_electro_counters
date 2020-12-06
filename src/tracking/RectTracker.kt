package tracking

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.video.SparsePyrLKOpticalFlow

class RectTracker {
    data class Result(val nextBoxes: List<Rect>, val statuses: List<Boolean>)

    fun track(prevImg: Mat, nextImg: Mat, prevBoxes: List<Rect>): Result {
        if (prevBoxes.isEmpty()) {
            return Result(listOf(), listOf())
        }
        val prevPts = prevBoxes.toTrackerPts()
        val (nextPts, statusesMat) = trackPoints(prevImg, nextImg, prevPts)
        assert(nextPts.rows() == statusesMat.rows())
        assert(nextPts.rows() % 2 == 0)
        val nextBoxes = ArrayList<Rect>()
        val statuses = ArrayList<Boolean>()

        val iter = toPointsAndStatuses(nextPts, statusesMat).iterator()
        while (iter.hasNext()) {
            val (point1, status1) = iter.next()
            val (point2, status2) = iter.next()
            nextBoxes.add(Rect(point1, point2))
            statuses.add(status1 && status2)
        }

        return Result(nextBoxes, statuses)
    }

    private companion object {
        private val optflow = SparsePyrLKOpticalFlow.create()

        fun trackPoints(prevImg: Mat, nextImg: Mat, prevPts: Mat): Pair<Mat, Mat> {
            val nextPts = Mat()
            val statuses = Mat()
            optflow.calc(prevImg, nextImg, prevPts, nextPts, statuses)
            return nextPts to statuses
        }

        fun toPointsAndStatuses(pts: Mat, statuses: Mat) = sequence {
            val ptsData = FloatArray(2)
            val statusData = ByteArray(1)
            for (i in 0 until pts.rows()) {
                pts.get(i, 0, ptsData)
                statuses.get(i, 0, statusData)

                val point = Point(ptsData[0].toDouble(), ptsData[1].toDouble())
                val status = statusData[0] == 1

                yield(point to status)
            }
        }

        fun List<Rect>.toTrackerPts(): Mat {
            val pts = Mat(size * 2, 2, CvType.CV_32F)
            for (boxIndex in 0 until size) {
                val tl = this[boxIndex].tl()
                var ptIndex = boxIndex + boxIndex //boxIndex*2
                pts.put(ptIndex, 0, tl.x)
                pts.put(ptIndex, 1, tl.y)

                val br = this[boxIndex].br()
                ptIndex++
                pts.put(ptIndex, 0, br.x)
                pts.put(ptIndex, 1, br.y)
            }
            return pts
        }
    }
}
