package ootbingo.barinade.bot.time.worker

import org.slf4j.LoggerFactory
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class Worker(
  tasks: List<WorkerTask>,
) {

  private val logger = LoggerFactory.getLogger(Worker::class.java)

  private val taskQueue = LinkedList(tasks)
  private var nextTask: WorkerTask?

  init {

    if (taskQueue.isEmpty()) {
      throw IllegalArgumentException("Worker must be initialized with at least one task")
    }

    if (!taskQueue.map { it.startAfter }.isSortedAscending()) {
      throw IllegalArgumentException("Tasks must be ordered by start time")
    }

    nextTask = taskQueue.poll()

    logger.info("Worker initialized")
  }

  @Suppress("NOTHING_TO_INLINE") // needed for mocking
  inline fun tick(elapsedTime: Duration) {
    tick(elapsedTime.inWholeMilliseconds)
  }

  fun tick(elapsedMillis: Long) {

    val elapsedTime = elapsedMillis.milliseconds
    val task = nextTask ?: return

    if (elapsedTime >= task.startAfter) {
      logger.info("Running task${task.name?.let { " $it" } ?: ""}")
      task.task.invoke()
      nextTask = taskQueue.poll()
    } else {
      logger.debug("Nothing to do at $elapsedMillis ms")
    }
  }

  fun hasMoreTasks(): Boolean = nextTask != null

  private fun <T : Comparable<T>> List<T>.isSortedAscending(): Boolean {
    return asSequence().zipWithNext { a, b -> a <= b }.all { it }
  }
}
