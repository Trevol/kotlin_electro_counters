package com.tavrida.counter_scanner.aggregation

import org.opencv.core.Rect2d

data class AggregatedDetections(val box: Rect2d, val score: Float, val digitsCounts: List<DigitCount>)

