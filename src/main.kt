import nu.pattern.OpenCV
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import types.AggregatedDetections
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
        return frames(path)
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
                prevDetections = digitDetectionTracker.track(prevFrameGray!!, gray, prevDetections)
            }
            val result = digitExtractor.extract(currentDetections, prevDetections)
            prevDetections = result.aggregatedDetections
            val digitsAtPoints = result.digitsAtBoxes
            prevFrameGray = gray

            for (det in currentDetections) {
                Imgproc.rectangle(bgr, det.boxInImage, Scalar(0, 255, 0), 1)
            }
            HighGui.imshow("bgr", bgr)
            if (HighGui.waitKey(0) == 27)
                break
        }

    }
}

fun main() {
    PrototypeApp().run()
    exitProcess(0)
}

