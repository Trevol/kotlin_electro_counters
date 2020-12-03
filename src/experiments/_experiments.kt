fun main() {
    val mm = mapOf(
        0 to Pair("00", "0_0"),
        1 to Pair("11", "1_1")
    )
    // for ((k, v) in mm) {
    //     println("-----------------")
    //     println(k)
    //     println(v)
    // }

    mm
        // .map { entry ->
        //     val (k, v) = entry
        //     entry
        // }
        .map { index, values -> index to values }
        .forEach { println(it) }
}

fun <K, V, R> Map<out K, V>.map(transform: (K, V) -> R) =
    this.map { entry -> transform(entry.key, entry.value) }
