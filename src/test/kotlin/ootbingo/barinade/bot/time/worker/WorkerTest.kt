package ootbingo.barinade.bot.time.worker

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class WorkerTest {

  @Test
  internal fun throwsIfTaskListIsEmpty() {
    assertThrows<Exception> { Worker(listOf()) }
  }

  @Test
  internal fun throwsIfTaskListIsNotOrdered() {

    assertThrows<Exception> {
      Worker(listOf(
          WorkerTask(3.seconds) {},
          WorkerTask(2.seconds) {},
          WorkerTask(1.seconds) {},
      ))
    }

    assertThrows<Exception> {
      Worker(listOf(
          WorkerTask(42.seconds) {},
          WorkerTask(60.seconds) {},
          WorkerTask(50.seconds) {},
      ))
    }

    assertDoesNotThrow {
      Worker(listOf(
          WorkerTask(1.seconds) {},
          WorkerTask(1.seconds) {},
          WorkerTask(1.seconds) {},
      ))
    }
  }

  @Test
  internal fun callsTaskFunctionsWhenCorrectlyOrdered() {

    val completedTasks = mutableListOf<Int>()

    val soft = SoftAssertions()

    val tasks = listOf(
        WorkerTask(10.seconds) { completedTasks.add(1) },
        WorkerTask(42.seconds) { completedTasks.add(2) },
        WorkerTask(42.seconds) { completedTasks.add(3) },
        WorkerTask(99.seconds) { completedTasks.add(4) },
    )

    val worker = Worker(tasks)

    fun thenTasksAreCompleted(time: Duration, vararg tasks: Int) {
      soft.assertThat(completedTasks).`as`("After ${time.inWholeSeconds} seconds").containsExactly(*tasks.toTypedArray())
    }

    fun thenWorkerHasUncompletedTasks(time: Duration, expected: Boolean = true) {
      soft.assertThat(worker.hasMoreTasks()).`as`("After ${time.inWholeSeconds} seconds").isEqualTo(expected)
    }

    worker.tick(4.seconds)
    thenTasksAreCompleted(4.seconds)
    thenWorkerHasUncompletedTasks(4.seconds)
    worker.tick(9.seconds)
    thenTasksAreCompleted(9.seconds)
    thenWorkerHasUncompletedTasks(9.seconds)

    worker.tick(10.seconds)
    thenTasksAreCompleted(10.seconds, 1)
    thenWorkerHasUncompletedTasks(10.seconds)

    worker.tick(41.seconds)
    thenTasksAreCompleted(41.seconds, 1)
    thenWorkerHasUncompletedTasks(41.seconds)

    worker.tick(42.seconds)
    thenTasksAreCompleted(42.seconds, 1, 2)
    thenWorkerHasUncompletedTasks(42.seconds)

    worker.tick(43.seconds)
    thenTasksAreCompleted(43.seconds, 1, 2, 3)
    thenWorkerHasUncompletedTasks(43.seconds)

    worker.tick(98.seconds)
    thenTasksAreCompleted(98.seconds, 1, 2, 3)
    thenWorkerHasUncompletedTasks(98.seconds)

    worker.tick(99.seconds)
    thenTasksAreCompleted(99.seconds, 1, 2, 3, 4)
    thenWorkerHasUncompletedTasks(99.seconds, false)

    soft.assertAll()
  }
}
