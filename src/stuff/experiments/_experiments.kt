package stuff.experiments

import com.tavrida.electro_counters.detection.DarknetDetector
import kotlinx.coroutines.*
import nu.pattern.OpenCV
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import com.tavrida.electro_counters.utils.bgr2rgb
import kotlin.system.measureTimeMillis

fun createDetector(): DarknetDetector {
    var cfgFile =
        "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counters/data/yolov3-tiny-2cls-320.cfg"
    var darknetModel =
        "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/src/counters/best_weights/yolov3-tiny-2cls/320/1/yolov3-tiny-2cls-320.weights"
    return DarknetDetector(cfgFile, darknetModel, 320, .5f, .4f)
}

fun testImg(): Mat {
    val fn = "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/1/" +
            "20201009165842_0003.jpg"
    // Mat(960, 1280, CvType.CV_8UC3)
    return Imgcodecs.imread(fn).bgr2rgb()
}

fun DarknetDetector.warmup(img: Mat, numOfWorkers: Int = 0): DarknetDetector {
    val d = this
    runBlocking {
        d.detect(img)
        if (numOfWorkers > 0) {
            repeat(numOfWorkers) {
                launch(Dispatchers.Default) {
                    d.detect(img)
                    d.detect(img)
                }
            }
        }
    }
    return d
}

fun main_detectorPerWorker() {
    val testImg = testImg()
    val numOfWorkers = 6
    val numOfIterations = 500

    println("detectorPerWorker numOfWorkers=$numOfWorkers, numOfIterations=$numOfIterations")

    runBlocking {
        for (nw in 1..numOfWorkers) {
            println("NumOfWorkers: $nw")
            coroutineScope {
                repeat(nw) {
                    val workerNum = it
                    var timings = listOf<Long>()
                    launch(Dispatchers.Default) {
                        val detector = createDetector().warmup(testImg, 2)
                        timings = (1..numOfIterations).map { measureTimeMillis { detector.detect(testImg) } }
                    }.invokeOnCompletion {
                        println("worker $workerNum done. Min: ${timings.minOrNull()} Max: ${timings.maxOrNull()} Avg: ${timings.average()}")
                    }
                }
            }
        }
    }
    println("DONE")
}

fun main_singleDetector() {
    val testImg = testImg()
    val numOfWorkers = 6
    val numOfIterations = 500

    println("singleDetector numOfWorkers=$numOfWorkers, numOfIterations=$numOfIterations")

    val detector = createDetector().warmup(testImg, 0)

    runBlocking {
        for (nw in 1..numOfWorkers) {
            println("NumOfWorkers: $nw")
            coroutineScope {
                repeat(nw) {
                    val workerNum = it
                    var timings = listOf<Long>()
                    launch(Dispatchers.Default) {
                        timings = (1..numOfIterations).map { measureTimeMillis { detector.detect(testImg) } }
                    }.invokeOnCompletion {
                        println("worker $workerNum done. Min: ${timings.minOrNull()} Max: ${timings.maxOrNull()} Avg: ${timings.average()}")
                    }
                }
            }
        }
    }
    println("DONE")
}

fun main_detectorPool() {
    val testImg = testImg()
    val numOfWorkers = 6
    val numOfIterations = 500

    println("detectorPool numOfWorkers=$numOfWorkers, numOfIterations=$numOfIterations")

    val detectorPool = (1..numOfWorkers).map { createDetector().warmup(testImg, 2) }

    runBlocking {
        for (nw in 1..numOfWorkers) {
            println("NumOfWorkers: $nw")
            coroutineScope {
                repeat(nw) {
                    val workerNum = it
                    var timings = listOf<Long>()
                    launch(Dispatchers.Default) {
                        val detector = detectorPool[workerNum]
                        timings = (1..numOfIterations).map { measureTimeMillis { detector.detect(testImg) } }
                    }.invokeOnCompletion {
                        println("worker $workerNum done. Min: ${timings.minOrNull()} Max: ${timings.maxOrNull()} Avg: ${timings.average()}")
                    }
                }
            }
        }
    }
    println("DONE")
}

fun main() {
    OpenCV.loadLocally()
    main_singleDetector()
    main_detectorPerWorker()
    main_detectorPool()
}

