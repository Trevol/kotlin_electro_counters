package com.tavrida.electro_counters

import com.tavrida.electro_counters.aggregation.AggregatingBoxGroupingDigitExtractor
import com.tavrida.electro_counters.detection.DigitDetectionResult
import com.tavrida.electro_counters.detection.TwoStageDigitsDetector
import com.tavrida.electro_counters.tracking.AggregatedDigitDetectionTracker
import com.tavrida.electro_counters.types.AggregatedDetections
import com.tavrida.electro_counters.types.DigitAtBox
import com.tavrida.electro_counters.utils.rgb2gray
import org.opencv.core.Mat

class CounterReadingScanner(val detector: TwoStageDigitsDetector) {
    private var prevGrayImg: Mat? = null
    val digitExtractor = AggregatingBoxGroupingDigitExtractor()
    val digitDetectionTracker = AggregatedDigitDetectionTracker()
    var aggregatedDetections = listOf<AggregatedDetections>()

    fun scan(rgbImg: Mat): ScanResult {
        val currentDetections = detector.detect(rgbImg)?.digitsDetections ?: listOf()
        val grayImg = rgbImg.rgb2gray()
        if (prevGrayImg != null) {
            aggregatedDetections = digitDetectionTracker.track(prevGrayImg!!, grayImg, aggregatedDetections)
        }
        prevGrayImg = grayImg

        val extractionResult = digitExtractor.extract(currentDetections, aggregatedDetections)
        aggregatedDetections = extractionResult.aggregatedDetections
        return ScanResult(currentDetections, extractionResult.digitsAtBoxes, aggregatedDetections)
    }
}

data class ScanResult(
    val currentDetections: List<DigitDetectionResult>,
    val digitsAtPoints: List<DigitAtBox>,
    val aggregatedDetections: List<AggregatedDetections>
)