package tests

import nu.pattern.OpenCV
import org.opencv.core.*
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import tracking.AggregatedDigitDetectionTracker
import tracking.RectTracker
import types.AggregatedDetections
import types.DigitCount
import utils.FrameResult
import utils.Scalar
import utils.frames
import kotlin.system.exitProcess
import utils.println

private fun frames__(): Pair<Mat, Mat> {
    val path = "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/1/"
    val name = "20201009165842_0002.jpg"
    val f1 = Imgcodecs.imread(path + name, Imgcodecs.IMREAD_GRAYSCALE)
    val f2 = Imgcodecs.imread(path + name, Imgcodecs.IMREAD_GRAYSCALE)
    return f1 to f2
}

fun frames(id: Int): Sequence<FrameResult> {
    val path = "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/" +
            "$id/*.jpg"
    return frames(path)
}


fun main_track_AggregatedDetections() {
    //Track objects with rectangles
    OpenCV.loadLocally()

    val tracker = AggregatedDigitDetectionTracker()

    val box = Rect(600, 370, 20, 27)
    var detections = listOf(
        AggregatedDetections(box, .8f, listOf(DigitCount(2, 1)))
    )
    var prevGray: Mat? = null

    for ((index, bgr, rgb, gray) in frames(1)) {
        if (prevGray != null) {
            detections = tracker.track(gray, prevGray, detections)
        }
        prevGray = gray


        detections.forEach {
            Imgproc.rectangle(bgr, it.box, Scalar(0, 255, 0))
        }
        HighGui.imshow("dd", bgr)
        if (HighGui.waitKey(0) == 27)
            break
    }

    exitProcess(0)
}


fun main_track_Boxes() {
    //Track rectangle
    OpenCV.loadLocally()

    val tracker = RectTracker()

    var boxes = listOf(
        Rect(600, 370, 20, 27)
    )

    var prevGray: Mat? = null

    for ((index, bgr, rgb, gray) in frames(1)) {
        if (prevGray != null) {
            val (nextBoxes, statuses) = tracker.track(prevGray, gray, boxes)
            boxes = nextBoxes
        }
        prevGray = gray


        boxes.forEach {
            Imgproc.rectangle(bgr, it, Scalar(0, 255, 0))
        }
        HighGui.imshow("dd", bgr)
        if (HighGui.waitKey(0) == 27)
            break
    }

    exitProcess(0)
}

fun main_track_Points() {
    //Track points
    OpenCV.loadLocally()


    val tracker = RectTracker()

    val frames = frames(1).iterator()
    var pts = listOf(
        Point(600.0, 370.0),
        Point(620.0, 397.0)
    )

    var prevGray = frames.next().gray
    for ((pos, bgr, rgb, gray) in frames) {

        val (nextPts, statuses) = tracker.trackPoints(prevGray, gray, pts)
        println("prevPts", pts)
        println("nextPts", nextPts)
        println("------------------------")
        pts = nextPts


        prevGray = gray

        for (pt in pts)
            Imgproc.circle(bgr, pt, 1, Scalar(0, 0, 255))

        HighGui.imshow("dd", bgr)
        if (HighGui.waitKey(0) == 27)
            break
    }

    exitProcess(0)
}

fun main() {
    main_track_Boxes()
    // main_track_Points()
// Может проблема в Rect(p1, p2)????
}




