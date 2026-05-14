package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@LilyModule
internal class VersionModule(
  private val versionProperties: VersionProperties,
  private val uptimeService: UptimeService,
) {

  @LilyCommand("version")
  fun buildInfo(command: Command): Answer<AnswerInfo>? {
    return Answer.ofText(versionProperties.let { "Version ${it.version} (${it.build})" })
  }

  @LilyCommand("uptime")
  fun uptime(command: Command): Answer<AnswerInfo>? {

    if (command.messageInfo !is DiscordMessageInfo) return null

    val (appBootTime, appUptime) = uptimeService.getAppUptime()
    val (systemBootTime, systemUptime) = uptimeService.getSystemUptime()

    return Answer.ofText(
      """
      App running since ${appBootTime.format()} ($appUptime)
      System running since ${systemBootTime.format()} ($systemUptime)
    """.trimIndent()
    )
  }

  private val datetimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")

  private fun Instant.format() = datetimeFormatter.format(toJavaInstant().atZone(ZoneId.systemDefault()))
}
