import com.tavrida.counter_scanner.detection.DarknetDetector
import com.tavrida.counter_scanner.detection.TwoStageDigitsDetector
import com.tavrida.counter_scanner.utils.*
import com.tavrida.counter_scanner.aggregation.AggregatedDetections
import com.tavrida.counter_scanner.aggregation.DigitAtBox
import com.tavrida.counter_scanner.scanning.nonblocking.NonblockingCounterReadingScanner
import nu.pattern.OpenCV
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import stuff.FrameResult
import stuff.frames
import kotlin.system.exitProcess

private class NonblockingPrototypeApp {
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

    fun createDetector(warmup: Boolean): TwoStageDigitsDetector {
        var cfgFile =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counters/data/yolov3-tiny-2cls-320.cfg"
        var darknetModel =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counters/best_weights/yolov3-tiny-2cls/320/1/yolov3-tiny-2cls-320.weights"
        val screenDetector = DarknetDetector(cfgFile, darknetModel, 320, .5f, .4f)

        cfgFile =
            "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counter_digits/data/yolov3-tiny-10cls-320.cfg"
        // darknetModel =
        //     "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counter_digits/best_weights/4/yolov3-tiny-10cls-320.4.weights"
        darknetModel =
            "/home/trevol/Repos/Android/android-electro-counters/app/src/main/assets/yolov3-tiny-10cls-320.4.weights"
        val digitsDetector = DarknetDetector(cfgFile, darknetModel, 320, .5f, .4f)

        if (warmup) {
            warmup(screenDetector)
            warmup(digitsDetector)
        }
        return TwoStageDigitsDetector(screenDetector, digitsDetector)
    }

    fun warmup(detector: DarknetDetector) {
        detector.detect(Mat(100, 320, CvType.CV_8UC3))
    }

    fun run() {
        val pathId = 1

        val detector = createDetector(warmup = true)
        val scanner = NonblockingCounterReadingScanner(detector)

        val desiredPos = 0
        val printEveryPos = 1
        for ((framePos, fn, bgr, rgb) in frames(pathId)) {
            val msOfStart = System.currentTimeMillis()

            val (digitsAtPoints, aggregatedDetections) = scanner.scan(rgb)

            show(bgr, framePos, digitsAtPoints, aggregatedDetections)

            val cycleDuration = System.currentTimeMillis() - msOfStart

            val showResult = HighGui.waitKey((30 - cycleDuration).toInt())
            if (showResult == 27)
                break
        }
        scanner.close()
    }

    val greenBgr = Scalar(0, 255, 0)
    val redBgr = Scalar(0, 0, 255)
    val digitRenderer = DigitRenderer(15, Imgproc.FONT_HERSHEY_SIMPLEX)
    private fun show(
        bgr: Mat,
        pos: Int,
        digitsAtPoints: Iterable<DigitAtBox>,
        aggregatedDetections: Iterable<AggregatedDetections>
    ) {
        val digitsImg = bgr.copy()
        val aggregatedDetectionsImg = bgr.copy()

        for (d in digitsAtPoints) {
            digitRenderer.render(digitsImg, d.digit, d.box.center(), greenBgr)
        }
        for (d in aggregatedDetections) {
            Imgproc.rectangle(aggregatedDetectionsImg, d.box.toRect(), greenBgr)
        }

        HighGui.imshow("digits", digitsImg)
        HighGui.imshow("detections", bgr)
        HighGui.imshow("aggregated", aggregatedDetectionsImg)
    }
}

fun main() {
    NonblockingPrototypeApp().run()
    exitProcess(0)
}

