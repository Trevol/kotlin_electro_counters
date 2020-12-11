import com.tavrida.electro_counters.aggregation.AggregatingBoxGroupingDigitExtractor
import com.tavrida.electro_counters.detection.DarknetDetector
import com.tavrida.electro_counters.detection.DigitDetectionResult
import com.tavrida.electro_counters.detection.TwoStageDigitsDetector
import com.tavrida.electro_counters.tracking.AggregatedDigitDetectionTracker
import com.tavrida.electro_counters.types.AggregatedDetections
import com.tavrida.electro_counters.types.DigitAtBox
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import com.tavrida.electro_counters.utils.*
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
        return com.tavrida.electro_counters.utils.frames(path)
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
        // TODO("sync packages with android project")
        // TODO("make class AggregatingSequentialDetector. Store and update state (aggregatedDetections, prevFrame)")
        val pathId = 1
        val detector = createDetector()
        val digitExtractor = AggregatingBoxGroupingDigitExtractor()
        val digitDetectionTracker = AggregatedDigitDetectionTracker()

        var aggregatedDetections = listOf<AggregatedDetections>()
        var prevFrameGray: Mat? = null

        val desiredPos = 114

        for ((framePos, bgr, rgb, gray) in frames(pathId)) {
            val currentDetections = detector.detect(rgb)?.digitsDetections ?: listOf()

            if (aggregatedDetections.isNotEmpty()) {
                aggregatedDetections = digitDetectionTracker.track(prevFrameGray!!, gray, aggregatedDetections)
            }
            val result = digitExtractor.extract(currentDetections, aggregatedDetections)

            aggregatedDetections = result.aggregatedDetections
            val digitsAtPoints = result.digitsAtBoxes
            prevFrameGray = gray

            if (framePos % 20 == 0) {
                println("framePos::", framePos)
            }
            if (framePos >= desiredPos) {
                val showResult = show(bgr, framePos, currentDetections, digitsAtPoints, aggregatedDetections)
                if (showResult == 27)
                    break
            }
        }
    }

    val greenBgr = Scalar(0, 255, 0)
    val redBgr = Scalar(0, 0, 255)
    val digitRenderer = DigitRenderer(15, Imgproc.FONT_HERSHEY_SIMPLEX)
    private fun show(
        bgr: Mat,
        pos: Int,
        currentDetections: Iterable<DigitDetectionResult>,
        digitsAtPoints: Iterable<DigitAtBox>,
        aggregatedDetections: Iterable<AggregatedDetections>
    ): Int {
        val digitsImg = bgr.copy()
        val aggregatedDetectionsImg = bgr.copy()

        for (det in currentDetections) {
            val box = det.boxInImage.toRect()
            Imgproc.rectangle(bgr, box, greenBgr, 1)
            // Imgproc.rectangle(aggregatedDetectionsImg, box, redBgr, 1)
        }
        for (d in digitsAtPoints) {
            digitRenderer.render(digitsImg, d.digit, d.box.center(), greenBgr)
        }
        for (d in aggregatedDetections) {
            Imgproc.rectangle(aggregatedDetectionsImg, d.box.toRect(), greenBgr)
        }

        HighGui.imshow("digits", digitsImg)
        // HighGui.imshow("detections", bgr)
        HighGui.imshow("aggregated", aggregatedDetectionsImg)
        return HighGui.waitKey(0)
    }
}

fun main() {
    PrototypeApp().run()
    exitProcess(0)
}

