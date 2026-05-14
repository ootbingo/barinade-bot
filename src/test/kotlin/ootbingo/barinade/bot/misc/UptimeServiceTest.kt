package ootbingo.barinade.bot.misc

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.OffsetTime
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

internal class UptimeServiceTest {

  //<editor-fold desc="Setup">

  private val appLaunchTime = Instant.fromEpochSeconds(Random.nextLong(1337, 123456789))
  private var currentTime = appLaunchTime
  private val commandExecutorMock: CommandExecutor = mock()
  private val clockMock = object : Clock {
    override fun now(): Instant = currentTime
  }

  private val service = UptimeService(clockMock, commandExecutorMock)

  private lateinit var returnedUptime: UptimeService.Uptime

  @BeforeEach
  internal fun setup() {
    givenCommandReturns("2026-05-14 16:17:00")
  }

  //</editor-fold>

  @Test
  internal fun returnsCorrectAppUptime() {

    val uptime = Random.nextLong(42, 42000).seconds

    givenCurrentTime(appLaunchTime + uptime)

    whenAppUptimeIsQueried()

    thenBootTimeIsEqualTo(appLaunchTime)
    thenUptimeIsEqualTo(uptime)
  }

  @Test
  internal fun queriesSystemBootTimeWhenSystemUptimeIsQueried() {

    whenSystemUptimeIsQueried()

    thenCommandIsExecuted(listOf("uptime", "-s"))
  }

  @ParameterizedTest
  @CsvSource(
    "1778767409,2026-05-14 13:59:35,2026-05-14T13:59:35.000Z,PT3M54S",
    "1778767409,2026-04-28 07:42:37,2026-04-28T07:42:37.000Z,P16DT6H20M52S",
  )
  internal fun returnsCorrectSystemUptime(
    currentTime: Long,
    systemBootTimeAsString: String,
    expectedBootTimeAsIsoString: String,
    expectedUptimeAsIsoString: String,
  ) {

    givenCurrentTime(Instant.fromEpochSeconds(currentTime))
    givenCommandReturns("$systemBootTimeAsString\n")

    whenSystemUptimeIsQueried()

    val expectedBootTimeInSystemTimezone = Instant.parse(expectedBootTimeAsIsoString)
      .toJavaInstant()
      .atZone(ZoneOffset.UTC)
      .toLocalDateTime()
      .atZone(ZoneOffset.systemDefault())
      .toInstant()
      .toKotlinInstant()

    val expectedUptimeInSystemTimezone =
      Duration.parse(expectedUptimeAsIsoString) + OffsetTime.now().offset.totalSeconds.seconds

    thenBootTimeIsEqualTo(expectedBootTimeInSystemTimezone)
    thenUptimeIsEqualTo(expectedUptimeInSystemTimezone)
  }

  //<editor-fold desc="Given">

  private fun givenCurrentTime(instant: Instant) {
    currentTime = instant
  }

  private fun givenCommandReturns(result: String) {
    whenever(commandExecutorMock.execute(any())).thenReturn(result)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenAppUptimeIsQueried() {
    returnedUptime = service.getAppUptime()
  }

  private fun whenSystemUptimeIsQueried() {
    returnedUptime = service.getSystemUptime()
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenBootTimeIsEqualTo(expectedBootTime: Instant) {
    assertThat(returnedUptime.bootTime).isEqualTo(expectedBootTime)
  }

  private fun thenUptimeIsEqualTo(expectedUptime: Duration) {
    assertThat(returnedUptime.uptime).isEqualTo(expectedUptime)
  }

  private fun thenCommandIsExecuted(expectedCommand: List<String>) {
    verify(commandExecutorMock).execute(expectedCommand)
  }

  //</editor-fold>
}
