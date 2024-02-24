package ootbingo.barinade.bot.time.worker

import kotlin.time.Duration

data class WorkerTask(
  val startAfter: Duration,
  val task: () -> Unit,
)
