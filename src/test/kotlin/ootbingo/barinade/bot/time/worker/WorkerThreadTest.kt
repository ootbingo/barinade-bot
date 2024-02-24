package ootbingo.barinade.bot.time.worker

import ootbingo.barinade.bot.time.SleepFunction
import ootbingo.barinade.bot.time.ticker.TickerMock
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class WorkerThreadTest {

  //<editor-fold desc="Setup">

  private val workerMock = mock<Worker> {
    whenever(it.hasMoreTasks()).thenReturn(false)
  }

  private val tickerMock = TickerMock()
  private val tasks = listOf(42, 1701).map { WorkerTask(it.seconds) {} }
  private val workerFactoryMock = mock<WorkerFactory> { whenever(it.createWorker(any())).thenReturn(workerMock) }
  private val sleepFunctionMock: SleepFunction = mock(name = "Henk") //{
//    doAnswer { i -> println("Mock sleep ${i.arguments[0]} ms") }.whenever(it).sleep(any())
//  }

  private val thread = WorkerThread(
    "UnitTest",
    tickerMock,
    tasks,
    workerFactoryMock,
    sleepFunctionMock,
  )

  //</editor-fold>

  @Test
  internal fun createsWorkerWithTasks() {
    verify(workerFactoryMock).createWorker(tasks)
  }

  @Test
  internal fun terminatesAfterLastTask() {

    val duration = Random.nextInt(42..1337).seconds
    val numberOfTasks = Random.nextInt(3..7)

    givenWorkerHasTasks(numberOfTasks)

    whenTimeElapses(duration)
    whenFullThreadIsRun()

    thenSleepIsCalledTimes(numberOfTasks)
    thenWorkerIsCalledWithTicks(List(numberOfTasks) { _ -> duration })
  }

  @Test
  internal fun sleeps200MillisecondsPerTick() {

    whenTickIsRun()

    thenThreadSleepsFor(200.milliseconds)
  }

  @Test
  internal fun callsWorkerOnEveryTick() {

    whenTickIsRun()
    thenWorkerWasLastCalledAtTime(0.seconds)

    whenTimeElapses(3.minutes)
    whenTickIsRun()
    thenWorkerWasLastCalledAtTime(3.minutes)

    whenTimeElapses(42.seconds)
    whenTickIsRun()
    thenWorkerWasLastCalledAtTime(222.seconds)

    whenTimeElapses(1.hours + 18.seconds)
    whenTickIsRun()
    thenWorkerWasLastCalledAtTime(64.minutes)
  }

  //<editor-fold desc="Given">

  //</editor-fold>

  //<editor-fold desc="When">

  private fun givenWorkerHasTasks(numberOfTasks: Int) {

    assert(numberOfTasks >= 2)

    whenever(workerMock.hasMoreTasks())
      .thenReturn(true, *List(numberOfTasks - 1) { true }.toTypedArray(), false)
  }

  private fun whenFullThreadIsRun() {
    Executors.newSingleThreadExecutor().run {
      execute(thread)
      shutdown()
    }
  }

  private fun whenTickIsRun() {
    tickerMock.start()
    thread.runSingleTick()
  }

  private fun whenTimeElapses(elapsedTime: Duration) {
    tickerMock.advanceBy(elapsedTime)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenSleepIsCalledTimes(expectedSleepCount: Int) {
    Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted {
      verify(sleepFunctionMock, times(expectedSleepCount)).sleep(any())
    }
  }

  private fun thenThreadSleepsFor(expectedDuration: Duration) {
    verify(sleepFunctionMock).sleep(expectedDuration.inWholeMilliseconds)
  }

  private fun thenWorkerIsCalledWithTicks(expectedTicks: List<Duration>) {
    verify(workerMock, atLeast(0)).hasMoreTasks()
    Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted {
      val captor = argumentCaptor<Long>()
      verify(workerMock, times(expectedTicks.size)).tick(captor.capture())
      assertThat(captor.allValues.map { it.milliseconds }).containsExactlyElementsOf(expectedTicks)
    }
  }

  private fun thenWorkerWasLastCalledAtTime(expectedDuration: Duration) {
    val captor = argumentCaptor<Long>()
    verify(workerMock, atLeastOnce()).tick(captor.capture())
    assertThat(captor.lastValue).isEqualTo(expectedDuration.inWholeMilliseconds)
  }

  //</editor-fold>
}
