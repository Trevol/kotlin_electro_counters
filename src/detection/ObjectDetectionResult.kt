package detection

import org.opencv.core.Rect2d

data class ObjectDetectionResult(
    val classId: Int,
    val classScore: Float,
    val box: Rect2d,
    val normalizedBox: Rect2d
)