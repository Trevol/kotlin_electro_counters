package stuff.experiments

import java.util.concurrent.*
import kotlin.concurrent.thread

fun simulateWork(workDurationInMs: Long) {
    val start = System.currentTimeMillis()
    var i = 999999999999.999999999
    while (System.currentTimeMillis() - start < workDurationInMs) {
        repeat(2000) { i *= .99999999999 }
    }
}

inline fun isRunning() = !isInterrupted()
inline fun isInterrupted() = Thread.currentThread().isInterrupted

fun <E> BlockingQueue<E>.takeAll(): List<E> {
    val first = take()
    return mutableListOf<E>(first).also { this.drainTo(it) }
}

inline fun <E> BlockingQueue<E>.takeLast() = takeAll().last()

inline fun <E> BlockingQueue<E>.keepLast(): E? {
    val c = mutableListOf<E>()
    drainTo(c)
    return c.lastOrNull()
}


//-------------------------------------------------
private class Mat {
    fun toGray() = this
}

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
    if (prevDetections.isEmpty()) {
        return listOf()
    }
    return prevDetections
}

private fun track(
    imageSequence: List<Mat>,
    prevDetections: List<AggregatedDetections>
): List<AggregatedDetections> {
    if (prevDetections.isEmpty()) {
        return listOf()
    }
    val first = imageSequence.first()
    val next = imageSequence.subList(1, imageSequence.lastIndex)
    return track(first, next, prevDetections)
}

private fun track(
    prevImg: Mat,
    nextImg: Mat,
    prevDetections: List<AggregatedDetections>
): List<AggregatedDetections> {
    if (prevDetections.isEmpty()) {
        return listOf()
    }
    return prevDetections
}

private fun postToUIUpdate(rgb: Mat, detections: List<AggregatedDetections>) = Unit

private data class SerialGrayItem(val serialId: Int, val gray: Mat)

private fun List<SerialGrayItem>.bySerialId(serialId: Int): List<SerialGrayItem> {
    return this.filter { it.serialId >= serialId }
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

        var serialSeq = 0
        val prevFrameItems = mutableListOf<SerialGrayItem>()
        var actualDetections = listOf<AggregatedDetections>()

        while (true) {
            val msOfStart = System.currentTimeMillis()

            val rgb = Mat()
            val gray = rgb.toGray()
            detectorJobInput.put(DetectorJobInputItem(serialSeq, rgb, gray))
            if (prevFrameItems.isEmpty()) {
                continue //special processing of first frame (no prev frame and detections to continue processing - so skipping it)
            }
            val detectorResult = detectorJobOutput.keepLast()
            if (detectorResult != null) {
                val frames = prevFrameItems.bySerialId(detectorResult.serialId)
                    .map { it.gray }
                    .toMutableList()
                frames.add(gray)
                actualDetections = track(frames, detectorResult.detections)
                prevFrameItems.clear()
            } else {
                val prevGray = prevFrameItems.last().gray
                actualDetections = track(prevGray, gray, actualDetections)
            }

            prevFrameItems.add(SerialGrayItem(serialSeq, gray))

            serialSeq++

            postToUIUpdate(rgb, actualDetections)

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