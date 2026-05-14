package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import ootbingo.barinade.bot.misc.UptimeService.*
import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinInstant

internal class VersionModuleTest : ModuleTest() {

  private val properties = VersionProperties()
  private val uptimeServiceMock: UptimeService = mock()

  private val module = VersionModule(properties, uptimeServiceMock)

  override val commands by lazy {
    mapOf(
      "version" to module::buildInfo,
      "uptime" to module::uptime,
    )
  }

  private lateinit var thenAnswer: Answer<AnswerInfo>

  @Test
  internal fun answersWithBuildInfoToDiscordMessage() {

    givenProperties {
      version = "42.123.0-TEST"
      build = "345agf"
    }

    "!version".sendAsDiscordMessage()

    thenAnswer hasVersion "42.123.0-TEST"
    thenAnswer hasBuild "345agf"
  }

  @Test
  internal fun answersWithUptimesToDiscordMessage() {

    givenServiceReturnsAppUptime(
      Uptime(
        LocalDateTime.of(2026, 5, 14, 16, 39, 12).atZone(ZoneOffset.systemDefault()).toInstant().toKotlinInstant(),
        42.days + 13.hours + 42.minutes + 37.seconds,
      )
    )

    givenServiceReturnsSystemUptime(
      Uptime(
        LocalDateTime.of(2025, 12, 24, 18, 24, 33).atZone(ZoneOffset.systemDefault()).toInstant().toKotlinInstant(),
        2.hours + 1.minutes + 19.seconds,
      )
    )

    "!uptime".sendAsDiscordMessage()

    thenAnswer hasUptime "App running since 2026-05-14 16:39:12 (42d 13h 42m 37s)"
    thenAnswer hasUptime "System running since 2025-12-24 18:24:33 (2h 1m 19s)"
  }

  //<editor-fold desc="Given">

  private fun givenProperties(block: VersionProperties.() -> Unit) {
    properties.apply(block)
  }

  private fun givenServiceReturnsAppUptime(uptime: Uptime) {
    whenever(uptimeServiceMock.getAppUptime()).thenReturn(uptime)
  }

  private fun givenServiceReturnsSystemUptime(uptime: Uptime) {
    whenever(uptimeServiceMock.getSystemUptime()).thenReturn(uptime)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun String.sendAsDiscordMessage() {
    thenAnswer = whenDiscordMessageIsSent("", this)!!
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private infix fun Answer<AnswerInfo>.hasVersion(version: String) {
    val versionPart = this.text.split(" ")[1]
    assertThat(versionPart).isEqualTo(version)
  }

  private infix fun Answer<AnswerInfo>.hasBuild(build: String) {
    val buildPart = this.text.split("(")[1].replace(")", "")
    assertThat(buildPart).isEqualTo(build)
  }

  private infix fun Answer<AnswerInfo>.hasUptime(expectedUptime: String) {
    assertThat(text.lines()).contains(expectedUptime)
  }

  //</editor-fold>
}
