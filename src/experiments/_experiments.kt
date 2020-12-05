package experiments

import nu.pattern.OpenCV
import org.opencv.core.*
import java.awt.geom.FlatteningPathIterator

fun main() {
    sequence { yield(1) }.toList()
}

fun printArgs(vararg args: Any) {
    for (arg in args) {
        print(arg)
        print(" ")
    }
}

fun println(vararg args: Any) {
    print(args)
    kotlin.io.println()
}
