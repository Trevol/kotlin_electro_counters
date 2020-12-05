import nu.pattern.OpenCV
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.video.SparsePyrLKOpticalFlow

fun main() {
    OpenCV.loadLocally()

    val fn1 =
        "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/1/20201009165842_0003.jpg"
    val fn2 =
        "/home/trevol/Repos/experiments_with_lightweight_detectors/electric_counters/images/smooth_frames/1/20201009165842_0004.jpg"

    val img1 = Imgcodecs.imread(fn1, Imgcodecs.IMREAD_GRAYSCALE)
    val img2 = Imgcodecs.imread(fn2, Imgcodecs.IMREAD_GRAYSCALE)
    val optflow = SparsePyrLKOpticalFlow.create()

    val prevPts = Mat(2, 2, CvType.CV_32F)
    prevPts.put(0, 0, 200.0)
    prevPts.put(0, 1, 200.0)
    prevPts.put(1, 0, 300.0)
    prevPts.put(1, 1, 300.0)
    val nextPts = Mat()
    val status = Mat()
    optflow.calc(img1, img2, prevPts, nextPts, status)
    assert(nextPts.rows() == status.rows())

    println(status.type())
    for (i in 0 until nextPts.rows()) {
        print(nextPts[i, 0][0])
        print(" ")
        print(nextPts[i, 1][0])
        print(" ")
        println(status[i, 0][0])
    }
}

main()