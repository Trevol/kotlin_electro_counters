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
import kotlin.concurrent.thread

class NonblockingCounterReadingScanner : Closeable {
    data class ScanResult(
        val digitsAtPoints: List<DigitAtBox>,
        val aggregatedDetections: List<AggregatedDetections>
    )

    var closed = false

    private val detectionTracker = AggregatedDigitDetectionTracker()
    private val digitExtractor = AggregatingBoxGroupingDigitExtractor()

    val detectorThread = startDetectorThread()

    override fun close() {
        detectorThread.interrupt() //should wait for cancelation???
        // TODO: clear frame queue and other state
        closed = true
        TODO()
    }

    fun scan(rgbImg: Mat): ScanResult {
        if (closed) {
            throw IllegalStateException("Scanner is closed")
        }
        TODO()
    }

    fun startDetectorThread() = thread {
        TODO()
    }

}

