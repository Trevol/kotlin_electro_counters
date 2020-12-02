import org.opencv.core.Mat
import org.opencv.core.Rect

data class TwoStageDigitDetectionResult(
    val counterBox: Rect?,
    val counterScore: Float?,
    val screenBox: Rect,
    val screenScore: Float,

    val digitsDetections: Collection<DigitDetectionResult>
)

data class DigitDetectionResult(val digit: Int, val score: Float, val boxInImage: Rect)