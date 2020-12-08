package experiments

import nu.pattern.OpenCV
import org.opencv.core.*

fun main() {
    OpenCV.loadLocally()
    val n = 1000
//    что быстрее:
//    1) проходить Mat в цикле
//    2) переместить содержимое в FloatArray и работать уже с массивом
    val pts = Mat(n, 2, CvType.CV_32F)

    print(pts.total())


}


