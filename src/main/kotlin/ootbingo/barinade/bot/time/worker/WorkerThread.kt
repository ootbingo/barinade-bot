package ootbingo.barinade.bot.time.worker

import ootbingo.barinade.bot.time.SleepFunction
import ootbingo.barinade.bot.time.ticker.Ticker
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.milliseconds

class WorkerThread(
  private val threadName: String,
  private val ticker: Ticker,
  tasks: List<WorkerTask>,
  workerFactory: WorkerFactory,
  private val sleep: SleepFunction,
) : Thread() {

  private val logger = LoggerFactory.getLogger(WorkerThread::class.java)
  private val worker = workerFactory.createWorker(tasks)

  override fun run() {

    currentThread().name = threadName

    logger.info("Starting worker")

    ticker.start()

    while (worker.hasMoreTasks()) {
      try {
        runSingleTick()
      } catch (e: Exception) {
        logger.error("Interrupted")
        return
      }
    }

    logger.info("Worker finished")
  }

  internal fun runSingleTick() {
    worker.tick(ticker.elapsedTime)
    sleep(200.milliseconds)
  }
}
