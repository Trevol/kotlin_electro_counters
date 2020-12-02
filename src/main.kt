import nu.pattern.OpenCV
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui
import kotlin.system.exitProcess

fun main() {
    OpenCV.loadLocally()
    val m = Mat(200, 200, CvType.CV_8UC3, Scalar(23.0, 189.0, 123.0))
    HighGui.imshow("123", m)
    HighGui.waitKey()
    exitProcess(0)
}