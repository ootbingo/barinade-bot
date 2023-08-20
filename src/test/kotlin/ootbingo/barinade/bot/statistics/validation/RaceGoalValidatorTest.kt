package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.statistics.validation.IdBlacklistRaceGoalValidator.IdType.*
import ootbingo.barinade.bot.statistics.validation.UrlRaceGoalValidator.*
import ootbingo.barinade.bot.statistics.validation.UrlRaceGoalValidator.GoalType.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class RaceGoalValidatorTest {

  //<editor-fold desc="Setup">

  private val urlValidatorMock = mock<UrlRaceGoalValidator>()
  private val dateValidatorMock = mock<DateRaceGoalValidator>()
  private val goalBlacklistValidatorMock = mock<GoalBlacklistRaceGoalValidator>()
  private val idBlacklistValidatorMock = mock<IdBlacklistRaceGoalValidator>()

  private val validator = RaceGoalValidator(
      urlValidatorMock,
      dateValidatorMock,
      goalBlacklistValidatorMock,
      idBlacklistValidatorMock,
  )

  private lateinit var thenRace: AtomicBoolean

  @BeforeEach
  internal fun setup() {
    givenUrlIs(GITHUB_IO_BINGO)
    givenValidDate()
    givenNoBlacklistedWords()
    givenNeutralId()
  }

  //</editor-fold>

  //<editor-fold desc="Test: URL">

  @Test
  internal fun validatesCorrectUrl() {

    val goal = UUID.randomUUID().toString()

    whenValidationRequested(goal = goal)

    thenUrlForValidationIsEqualTo(goal)
  }

  @ParameterizedTest
  @EnumSource(GoalType::class, names = ["NO_BINGO"], mode = EXCLUDE)
  internal fun bingoIfAnyValidType(goalType: GoalType) {

    givenUrlIs(goalType)
    givenValidDate()
    givenNoBlacklistedWords()
    givenNeutralId()

    whenValidationRequested()

    thenRace.isValidBingo()
  }

  @Test
  internal fun noBingoIfNoValidUrl() {

    givenInvalidUrl()
    givenValidDate()
    givenNoBlacklistedWords()
    givenNeutralId()

    whenValidationRequested()

    thenRace.isValidBingo(false)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Date">

  @Test
  internal fun validatesCorrectDateForSrlRace() {

    val date = Instant.now().minusSeconds(Random.nextLong(-10000, 10000))

    givenUrlIs(SRL_BINGO)

    whenValidationRequested(date = date)

    thenDateForSrlValidationIsEqualTo(date)
  }

  @Test
  internal fun bingoWhenDateIsValidForSrlRace() {

    givenValidDate()
    givenUrlIs(SRL_BINGO)
    givenNoBlacklistedWords()
    givenNeutralId()

    whenValidationRequested()

    thenRace.isValidBingo()
  }

  @Test
  internal fun noBingoWhenDateIsInvalidForSrlRace() {

    givenInvalidDate()
    givenUrlIs(SRL_BINGO)
    givenNoBlacklistedWords()
    givenNeutralId()

    whenValidationRequested()

    thenRace.isValidBingo(false)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Word Blacklist">

  @Test
  internal fun checkCorrectGoalForBlacklistedWords() {

    val goal = UUID.randomUUID().toString()

    whenValidationRequested(goal = goal)

    thenGoalForWordBlacklistCheckIsEqualTo(goal)
  }

  @ParameterizedTest
  @EnumSource(GoalType::class, names = ["NO_BINGO"], mode = EXCLUDE)
  internal fun noBingoWhenBlacklistedWord(goalType: GoalType) {

    givenBlacklistedWords()
    givenUrlIs(goalType)
    givenValidDate()
    givenNeutralId()

    whenValidationRequested()

    thenRace.isValidBingo(false)
  }

  //</editor-fold>

  //<editor-fold desc="Test: ID Blacklist">

  @Test
  internal fun checkCorrectIdForBlacklistedRaces() {

    val id = UUID.randomUUID().toString()

    whenValidationRequested(id = id)

    thenIdForIdBlacklistCheckIsEqualTo(id)
  }

  @ParameterizedTest
  @EnumSource(GoalType::class, names = ["NO_BINGO"], mode = EXCLUDE)
  internal fun noBingoWhenRaceIdBlacklisted(goalType: GoalType) {

    givenBlacklistedId()
    givenUrlIs(goalType)
    givenValidDate()
    givenNoBlacklistedWords()

    whenValidationRequested()

    thenRace.isValidBingo(false)
  }

  @Test
  internal fun isBingoWhenRaceIdWhitelisted() {

    givenWhitelistedId()
    givenInvalidUrl()
    givenInvalidDate()
    givenBlacklistedWords()

    whenValidationRequested()

    thenRace.isValidBingo()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenUrlIs(goalType: GoalType) {
    whenever(urlValidatorMock.validateGoal(any())).thenReturn(goalType)
  }

  private fun givenInvalidUrl() {
    whenever(urlValidatorMock.validateGoal(any())).thenReturn(NO_BINGO)
  }

  private fun givenValidDate() {
    whenever(dateValidatorMock.validateSrlBingoDate(any())).thenReturn(true)
  }

  private fun givenInvalidDate() {
    whenever(dateValidatorMock.validateSrlBingoDate(any())).thenReturn(false)
  }

  private fun givenNoBlacklistedWords() {
    whenever(goalBlacklistValidatorMock.validateGoal(any())).thenReturn(true)
  }

  private fun givenBlacklistedWords() {
    whenever(goalBlacklistValidatorMock.validateGoal(any())).thenReturn(false)
  }

  private fun givenBlacklistedId() {
    whenever(idBlacklistValidatorMock.validateRaceId(any())).thenReturn(BLACKLISTED)
  }

  private fun givenWhitelistedId() {
    whenever(idBlacklistValidatorMock.validateRaceId(any())).thenReturn(WHITELISTED)
  }

  private fun givenNeutralId() {
    whenever(idBlacklistValidatorMock.validateRaceId(any())).thenReturn(NEUTRAL)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenValidationRequested(id: String = "0", goal: String = "0", date: Instant = Instant.now()) {
    thenRace = AtomicBoolean(validator.isBingo(id, goal, date))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenUrlForValidationIsEqualTo(expectedGoal: String) {
    verify(urlValidatorMock).validateGoal(expectedGoal)
  }

  private fun thenDateForSrlValidationIsEqualTo(expectedDate: Instant) {
    verify(dateValidatorMock).validateSrlBingoDate(expectedDate)
  }

  private fun thenGoalForWordBlacklistCheckIsEqualTo(expectedGoal: String) {
    verify(goalBlacklistValidatorMock).validateGoal(expectedGoal)
  }

  private fun thenIdForIdBlacklistCheckIsEqualTo(expectedId: String) {
    verify(idBlacklistValidatorMock).validateRaceId(expectedId)
  }

  private fun AtomicBoolean.isValidBingo(bingo: Boolean = true) {
    assertThat(this.get()).isEqualTo(bingo)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private class RaceAssertion(
      private val validator: RaceGoalValidator = mock<RaceGoalValidator>(),
      var id: String = "0",
      var goal: String = "",
      var date: Instant = Instant.EPOCH,
  ) {

    fun isBingo(bingo: Boolean = true) {
      assertThat(validator.isBingo(id, goal, date)).isEqualTo(bingo)
    }
  }

  private fun assertRace(block: RaceAssertion.() -> Unit): RaceAssertion = RaceAssertion(validator).apply(block)

  private fun date(year: Int, month: Int, day: Int) =
      ZonedDateTime.of(year, month, day, 1, 1, 1, 0, ZoneId.of("UTC")).toInstant()

  //</editor-fold>
}
