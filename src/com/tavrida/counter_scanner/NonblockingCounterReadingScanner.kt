package com.tavrida.counter_scanner

import com.tavrida.counter_scanner.aggregation.AggregatedDetections
import com.tavrida.counter_scanner.aggregation.AggregatingBoxGroupingDigitExtractor
import com.tavrida.counter_scanner.aggregation.DigitAtBox
import com.tavrida.counter_scanner.detection.TwoStageDigitDetectionResult
import com.tavrida.counter_scanner.utils.rgb2gray
import com.tavrida.electro_counters.tracking.AggregatedDigitDetectionTracker
import kotlinx.coroutines.*
import org.opencv.core.Mat
import java.io.Closeable
import java.lang.IllegalStateException

class NonblockingCounterReadingScanner : Closeable {
    data class ScanResult(
        val digitsAtPoints: List<DigitAtBox>,
        val aggregatedDetections: List<AggregatedDetections>
    )

    var closed = false
    private var aggregatedDetections = listOf<AggregatedDetections>()
    private val queuedFrames = ArrayDeque<RgbWithGray>()

    private val detectionTracker = AggregatedDigitDetectionTracker()
    private val digitExtractor = AggregatingBoxGroupingDigitExtractor()
    private val detectionJob = startDetectionJob()

    override fun close() {
        detectionJob.cancel() //should wait for cancelation???
        // TODO: clear frame queue and other state
        closed = true
        TODO()
    }

    fun scan(rgbImg: Mat): ScanResult {
        if (closed) {
            throw IllegalStateException("Scanner is closed")
        }
        if (queuedFrames.isEmpty()) {
            return ScanResult(listOf(), listOf())
        }
        val gray = rgbImg.rgb2gray()
        aggregatedDetections = detectionTracker.track(queuedFrames.last().gray, gray, aggregatedDetections)
        queuedFrames.addLast(RgbWithGray(rgbImg, gray))
        return ScanResult(
            digitExtractor.extractDigits(aggregatedDetections),
            aggregatedDetections
        )
    }

    private fun startDetectionJob() = GlobalScope.launch(Dispatchers.Default) { detectionRoutine(this) }


    private fun detectionRoutine(coroutineScope: CoroutineScope) {
        while (coroutineScope.isActive) {
            // wait for image to detect
            //   - semaphore
            // how to wait for last (But) item arrival
        }
        TODO()
    }

    private fun detectionResultReady(detectionResult: TwoStageDigitDetectionResult) {
        // repropagate aggregated detec
        TODO()
    }
}

private data class RgbWithGray(val rgb: Mat, val gray: Mat)