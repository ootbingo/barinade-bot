package ootbingo.barinade.bot.time.worker

import ootbingo.barinade.bot.time.SleepFunction
import ootbingo.barinade.bot.time.ticker.TickerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class WorkerThreadFactory(
  private val tickerFactory: TickerFactory,
  private val workerFactory: WorkerFactory,
  private val sleepFunction: SleepFunction,
) {

  fun runWorkerThread(threadName: String, tasks: List<WorkerTask>) {

    val thread = WorkerThread(threadName, tickerFactory.createTicker(), tasks, workerFactory, sleepFunction)

    Executors.newSingleThreadExecutor().run {
      execute(thread)
      shutdown()
    }
  }
}
