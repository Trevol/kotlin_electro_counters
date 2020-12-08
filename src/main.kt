import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import tracking.AggregatedDigitDetectionTracker
import types.AggregatedDetections
import types.DigitAtBox
import utils.FrameResult
import utils.Scalar
import utils.copy
import kotlin.system.exitProcess

class PrototypeApp {
    companion object {
        init {
            OpenCV.loadLocally()
        }
    }

    fun frames(id: Int): Sequence<FrameResult> {
        val path = "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/" +
                "$id/*.jpg"
        return utils.frames(path)
    }

    fun createDetector(): TwoStageDigitsDetector {
        var cfgFile =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counters/data/yolov3-tiny-2cls-320.cfg"
        var darknetModel =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counters/best_weights/yolov3-tiny-2cls/320/1/yolov3-tiny-2cls-320.weights"
        val screenDetector = DarknetDetector(cfgFile, darknetModel, 320, .5f, .4f)

        cfgFile =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counter_digits/data/yolov3-tiny-10cls-320.cfg"
        darknetModel =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counter_digits/best_weights/4/yolov3-tiny-10cls-320.4.weights"
        val digitsDetector = DarknetDetector(cfgFile, darknetModel, 320, .5f, .4f)
        return TwoStageDigitsDetector(screenDetector, digitsDetector)
    }

    fun run() {
        val pathId = 1
        val detector = createDetector()
        val digitExtractor = AggregatingBoxGroupingDigitExtractor()
        val digitDetectionTracker = AggregatedDigitDetectionTracker()

        var prevDetections = listOf<AggregatedDetections>()
        var prevFrameGray: Mat? = null
        for ((index, bgr, rgb, gray) in frames(pathId)) {
            val currentDetections = detector.detect(rgb)?.digitsDetections ?: listOf()

            if (prevDetections.isNotEmpty()) {
                val oldSize = prevDetections.size
                prevDetections = digitDetectionTracker.track(prevFrameGray!!, gray, prevDetections)
                println("track!!! $oldSize ${prevDetections.size}")

            }
            val result = digitExtractor.extract(currentDetections, prevDetections)
            prevDetections = result.aggregatedDetections
            val digitsAtPoints = result.digitsAtBoxes
            prevFrameGray = gray

            val showResult = show(bgr, currentDetections, digitsAtPoints, prevDetections)
            if (showResult == 27)
                break
        }
    }

    val greenBgr = Scalar(0, 255, 0)
    private fun show(
        bgr: Mat,
        currentDetections: Iterable<DigitDetectionResult>,
        digitsAtPoints: Iterable<DigitAtBox>,
        aggregatedDetections: Iterable<AggregatedDetections>
    ): Int {
        val digitsImg = bgr.copy()
        val aggregatedDetectionsImg = bgr.copy()

        for (det in currentDetections) {
            Imgproc.rectangle(bgr, det.boxInImage, greenBgr, 1)
        }
        for (d in digitsAtPoints) {
            Imgproc.putText(
                digitsImg, d.digit.toString(), d.box.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, .5,
                greenBgr, 1
            )
        }
        for (d in aggregatedDetections) {
            Imgproc.rectangle(aggregatedDetectionsImg, d.box, greenBgr)
        }

        HighGui.imshow("digits", digitsImg)
        HighGui.imshow("detections", bgr)
        HighGui.imshow("aggregated", aggregatedDetectionsImg)
        return HighGui.waitKey(0)
    }
}

fun main() {
    PrototypeApp().run()
    exitProcess(0)
}

