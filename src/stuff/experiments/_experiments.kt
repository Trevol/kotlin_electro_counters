package stuff.experiments

fun main() {
    val r = listOf(1, 2, 3, 4, 5)
    println(r.lastIndex)
    println(r[r.lastIndex])
    println(r.subList(2, r.lastIndex+1))
}

