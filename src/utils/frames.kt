package utils

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import utils.glob
import java.io.File

fun frames(path: String) = sequence {
    glob(path)
        .sorted()
        .forEachIndexed { index, fn ->
            val bgr = Imgcodecs.imread(fn.absolutePath)
            val rgb = bgr.bgr2rgb()
            val gray = bgr.bgr2gray()
            yield(FrameResult(index, bgr, rgb, gray))
        }
}

data class FrameResult(val pos: Int, val bgr: Mat, val rgb: Mat, val gray: Mat)