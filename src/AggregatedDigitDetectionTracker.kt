import org.opencv.core.Mat
import org.opencv.video.SparsePyrLKOpticalFlow
import types.AggregatedDetections

class RectTracker {
    val optflow = SparsePyrLKOpticalFlow.create()

    private fun trackPoints(prevImg: Mat, nextImg: Mat, prevPts: Mat): Pair<Mat, Mat> {
        val nextPts = Mat()
        val status = Mat()
        optflow.calc(prevImg, nextImg, prevPts, nextPts, status)
        return nextPts to status
    }
}

class AggregatedDigitDetectionTracker {

    fun track(
        prevFrameGray: Mat,
        nextFrameGray: Mat,
        prevObjects: List<AggregatedDetections>
    ): List<AggregatedDetections> {
        throw NotImplementedError()
    }
}