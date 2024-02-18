package ootbingo.barinade.bot.time.ticker

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DefaultTickerTest {

  //<editor-fold desc="Setup">

  private val clockMock = mock<Clock>()

  private val ticker = DefaultTicker(clockMock)

  private var returnedDuration: Duration? = null
  private var caughtException: Exception? = null

  @BeforeEach
  internal fun setup() {
    whenever(clockMock.now()).thenReturn(Instant.DISTANT_PAST)
  }

  //</editor-fold>

  @Test
  internal fun throwsExceptionWhenElapsedTimeIsQueriedBeforeStarting() {

    whenElapsedTimeIsRead()

    thenExceptionIsThrown()
  }

  @Test
  internal fun tickerStartsAtZero() {

    givenTime(randomInstant())

    whenTickerIsStarted()
    whenElapsedTimeIsRead()

    thenReturnedDurationIsEqualTo(Duration.ZERO)
  }

  @Test
  internal fun tickerReturnsCorrectDuration() {

    val startTime = randomInstant()
    val milliseconds = Random.nextLong(42L, 42_000_000_000L).milliseconds

    givenTime(startTime)
    whenTickerIsStarted()

    givenTime(startTime + milliseconds)
    whenElapsedTimeIsRead()

    thenReturnedDurationIsEqualTo(milliseconds)
  }

  //<editor-fold desc="Given">

  fun givenTime(time: Instant) {
    whenever(clockMock.now()).thenReturn(time)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  fun whenTickerIsStarted() {
    ticker.start()
  }

  fun whenElapsedTimeIsRead() {
    try {
      returnedDuration = ticker.elapsedTime
    } catch (e: Exception) {
      caughtException = e
    }
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  fun thenReturnedDurationIsEqualTo(expectedDuration: Duration) {
    assertThat(returnedDuration).isEqualTo(expectedDuration)
  }

  fun thenExceptionIsThrown() {
    assertThat(caughtException).isNotNull()
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun randomInstant() =
    Instant.fromEpochMilliseconds(Random.nextLong(0, Instant.DISTANT_FUTURE.toEpochMilliseconds()))

  //</editor-fold>
}
