package ootbingo.barinade.bot.misc

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

@Service
internal class UptimeService(
  private val clock: Clock,
  private val commandExecutor: CommandExecutor,
) {

  private val appBootTime = clock.now()

  data class Uptime(val bootTime: Instant, val uptime: Duration)

  fun getAppUptime(): Uptime = Uptime(appBootTime, (clock.now() - appBootTime).truncate())

  fun getSystemUptime(): Uptime {

    val systemBootTimeAsString = commandExecutor.execute(listOf("uptime", "-s")).lines()[0]
    val systemBootTime = LocalDateTime.parse(systemBootTimeAsString, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"))
      .atZone(ZoneOffset.systemDefault())
      .toInstant()
      .toKotlinInstant()

    return Uptime(systemBootTime, (clock.now() - systemBootTime).truncate())
  }

  private fun Duration.truncate() = inWholeSeconds.seconds
}
