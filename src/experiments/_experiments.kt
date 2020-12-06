package experiments

import nu.pattern.OpenCV
import org.opencv.core.*

fun main() {
    OpenCV.loadLocally()
    val n = 1000
//    что быстрее:
//    1) проходить Mat в цикле
//    2) переместить содержимое в FloatArray и работать уже с массивом
    val ptsInput = FloatArray(n * 2) { it + 1f }
    val pts = Mat(n, 2, CvType.CV_32F)
    pts.put(0, 0, ptsInput)

    val ptsOutput = FloatArray(2)
    for(i in 0 until pts.rows()){
        pts.get(i, 0, ptsOutput)

        printlnArgs(ptsOutput[0], ptsOutput[1])

    }
}

fun printArgs(vararg args: Any) {
    for (arg in args) {
        print(arg)
        print(" ")
    }
}

fun printlnArgs(vararg args: Any) {
    printArgs(*args)
    println()
}
