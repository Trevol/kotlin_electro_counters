package tracking

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.video.SparsePyrLKOpticalFlow

class RectTracker {
    data class Result(val nextBoxes: List<Rect>, val status: List<Boolean>)
    fun track(prevImg: Mat, nextImg: Mat, prevBoxes: List<Rect>): Result {
        val prevPts = prevBoxes.toTrackerPts()
        val (nextPts, status) = trackPoints(prevImg, nextImg, prevPts)

        throw NotImplementedError()
    }

    private companion object {
        val optflow = SparsePyrLKOpticalFlow.create()

        private fun trackPoints(prevImg: Mat, nextImg: Mat, prevPts: Mat): Pair<Mat, Mat> {
            val nextPts = Mat()
            val status = Mat()
            optflow.calc(prevImg, nextImg, prevPts, nextPts, status)
            return nextPts to status
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