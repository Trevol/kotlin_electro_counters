package types

import org.opencv.core.Rect

data class DigitCount(val digit: Int, val count: Int)
data class AggregatedDetections(val box: Rect, val score: Float, val digitsCounts: List<DigitCount>)