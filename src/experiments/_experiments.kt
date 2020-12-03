import nu.pattern.OpenCV
import org.opencv.highgui.HighGui
import kotlin.system.exitProcess

fun main() {
    OpenCV.loadLocally()

    val path = "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/" +
            "1/*.jpg"
    for ((index, bgr, rgb, gray) in frames(path)) {
        HighGui.imshow("bgr", bgr)
        HighGui.imshow("rgb", rgb)
        HighGui.imshow("gray", gray)
        if (HighGui.waitKey(0) == 27)
            break
    }
    exitProcess(0)
}