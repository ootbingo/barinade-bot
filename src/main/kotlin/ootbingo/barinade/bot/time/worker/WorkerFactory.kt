package ootbingo.barinade.bot.time.worker

import org.springframework.stereotype.Component

@Component
class WorkerFactory {

  fun createWorker(tasks: List<WorkerTask>) = Worker(tasks)
}
