package stuff.experiments

import java.util.concurrent.*
import kotlin.concurrent.thread


fun main() {
    data class Item(val i: Int)

    val channel = LinkedBlockingQueue<Item>()
    channel.drainTo(mutableListOf())

    val t = thread(isDaemon = true) {
        while (true) {
            val items = channel.takeAll()
            val lastItem = items.last()
            println("$items --- $lastItem")
            simulateWork(1000)
        }
    }

    Thread.sleep(1000)
    (0..Int.MAX_VALUE).forEach {
        channel.put(Item(it))
        Thread.sleep(233)
    }
}