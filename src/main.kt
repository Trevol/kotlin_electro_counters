import nu.pattern.OpenCV
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui
import kotlin.system.exitProcess

class PrototypeApp {
    companion object {        init {
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
        for ((index, bgr, rgb, gray) in frames(pathId)) {
            val currentDetections = detector.detect(rgb)?.digitsDetections ?: listOf()

        }

    }
}

fun main() {
    PrototypeApp().run()
    exitProcess(0)
}

