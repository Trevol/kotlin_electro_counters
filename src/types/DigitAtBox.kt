package types

import org.opencv.core.Rect2d

data class DigitAtBox(val digit: Int, val box: Rect2d)