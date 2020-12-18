package stuff.experiments

import java.util.concurrent.*
import kotlin.concurrent.thread

fun simulateWork(workDurationInMs: Long) {
    val start = System.currentTimeMillis()
    var i = 0.0
    while (System.currentTimeMillis() - start < workDurationInMs) {
        repeat(2000) { i += .999 }
    }
}

inline fun isRunning() = !isInterrupted()
inline fun isInterrupted() = Thread.currentThread().isInterrupted

fun <E> BlockingQueue<E>.takeAll(): List<E> {
    val first = take()
    return mutableListOf<E>(first).also { this.drainTo(it) }
}

inline fun <E> BlockingQueue<E>.takeLast() = takeAll().last()


//-------------------------------------------------
private class Mat
private class DigitDetectionResult
private class AggregatedDetections

private fun aggregate(currentDetections: List<DigitDetectionResult>, prevDetections: List<AggregatedDetections>) =
    //TODO: precalculate digitWithMaxCount/totalCount at aggregation stage
    prevDetections

private fun detect(rgbFrame: Mat) = simulateWork(350).run {
    listOf(
        DigitDetectionResult(), DigitDetectionResult(), DigitDetectionResult()
    )
}

private fun track(
    prevImg: Mat,
    nextImgs: List<Mat>,
    prevDetections: List<AggregatedDetections>
): List<AggregatedDetections> {
    if (prevDetections.isEmpty()){
        return listOf()
    }
    return prevDetections
}

fun main() {
    data class DetectorJobInputItem(val serialId: Int, val rgb: Mat, val gray: Mat)
    data class DetectorJobOutputItem(val serialId: Int, val detections: List<AggregatedDetections>)

    val detectorJobInput = LinkedBlockingQueue<DetectorJobInputItem>()
    val detectorJobOutput = LinkedBlockingQueue<DetectorJobOutputItem>()

    fun detectorJob() {
        //NOTE: detectorJob is main source of aggregatedDetections/digitsAtBoxes
        var aggrDetectionsForFrame = listOf<AggregatedDetections>()
        var itemForDetection = detectorJobInput.takeLast()
        while (isRunning()) {
            val detectionsForFrame = detect(itemForDetection.rgb)
            if (isInterrupted()) { // can be interrupted during relatively long detection stage
                break
            }
            aggrDetectionsForFrame = aggregate(detectionsForFrame, aggrDetectionsForFrame)

            //TODO: may be exec in separate loop over inputItems - because propagation to multiple frames can take some time
            //TODO: and may be exec propagation in separate thread/job
            val frames = detectorJobInput.takeAll() // wait and take all items from channel
            aggrDetectionsForFrame = track(itemForDetection.gray, frames.map { it.gray }, aggrDetectionsForFrame)

            if (isInterrupted()) { // can be interrupted during relatively long detection stage
                break
            }
            itemForDetection = frames.last()
            detectorJobOutput.put(DetectorJobOutputItem(itemForDetection.serialId, aggrDetectionsForFrame))
        }
    }

    fun analyzeImageJob() {
        var ggg = 0
        class Inttt{
            fun put() = ggg+123
            fun get() = ggg
        }
        data class SerialGrayItem(val serialId: Int, val gray: Mat)
        var serialSeq = 0
        val prevItems = mutableListOf<SerialGrayItem>()

        while (true) {
            val msOfStart = System.currentTimeMillis()

            // TODO("Check in android app: check FPS if analyzeImage make some short (5, 10, 15, 20 ms) computation")
            val rgb = Mat()
            val gray = rgb
            detectorJobInput.put(DetectorJobInputItem(serialSeq, rgb, gray))

            // TODO("Need last item only. Where is no need for entire list of items")
            val detectionOutputItems = mutableListOf<DetectorJobOutputItem>()
            detectorJobOutput.drainTo(detectionOutputItems)
            if (detectionOutputItems.isEmpty()){
            //    work with local prevItems
            }
            else{
                TODO("detector send some results")
            }
            // track prev agg-detections (and digitsAtPoints??) to currentFrame

            prevItems.add(SerialGrayItem(serialSeq, gray))

            serialSeq++
            // post drawing and displaying to UI-thread

            val cycleDuration = System.currentTimeMillis() - msOfStart
            Thread.sleep(30 - cycleDuration) // simulate frame rate - one frame per 30ms
        }
    }

    thread(isDaemon = true) {
        try {
            detectorJob()
        } catch (e: InterruptedException) {
        }
    }
    Thread.sleep(50)
    analyzeImageJob()
}