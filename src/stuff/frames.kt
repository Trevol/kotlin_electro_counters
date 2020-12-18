package stuff

import com.tavrida.counter_scanner.utils.bgr2rgb
import com.tavrida.counter_scanner.utils.glob
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

fun frames(path: String) = sequence {
    glob(path)
        .sorted()
        .forEachIndexed { index, fn ->
            val bgr = Imgcodecs.imread(fn.absolutePath)
            val rgb = bgr.bgr2rgb()
            yield(FrameResult(index, fn, bgr, rgb))
        }
}

data class FrameResult(val pos: Int, val fn: File, val bgr: Mat, val rgb: Mat)