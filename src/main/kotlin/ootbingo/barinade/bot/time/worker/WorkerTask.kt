package ootbingo.barinade.bot.time.worker

import kotlin.time.Duration

data class WorkerTask(
  val startAfter: Duration,
  val name: String? = null,
  val task: () -> Unit,
)
