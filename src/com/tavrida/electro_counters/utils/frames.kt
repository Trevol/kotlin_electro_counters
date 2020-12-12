package com.tavrida.electro_counters.utils

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs

fun frames(path: String) = sequence {
    glob(path)
        .sorted()
        .forEachIndexed { index, fn ->
            val bgr = Imgcodecs.imread(fn.absolutePath)
            val rgb = bgr.bgr2rgb()
            yield(FrameResult(index, bgr, rgb))
        }
}

data class FrameResult(val pos: Int, val bgr: Mat, val rgb: Mat)