package com.tavrida.electro_counters.detection

import org.opencv.core.Rect2d

data class TwoStageDigitDetectionResult(
    val counterBox: Rect2d?,
    val counterScore: Float?,
    val screenBox: Rect2d,
    val screenScore: Float,

    val digitsDetections: List<DigitDetectionResult>
)

data class DigitDetectionResult(val digit: Int, val score: Float, val boxInImage: Rect2d)