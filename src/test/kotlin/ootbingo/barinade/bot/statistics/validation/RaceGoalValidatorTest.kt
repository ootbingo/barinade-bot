package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.properties.BingoRaceProperties
import ootbingo.barinade.bot.properties.model.WhitelistBingo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

class RaceGoalValidatorTest {

  private val validator = RaceGoalValidator()

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun isBingoWhenSrlUrl(prefix: String) {

    assertRace {
      goal = "http://${prefix}speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal"
      date = date(2018, 1, 1)
    }.isBingo()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun noBingoWhenSrlUrlAfterSwitch(prefix: String) {

    assertRace {
      goal = "http://${prefix}speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal"
      date = date(2019, 9, 21)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun isBingoWhenSrlUrlWithVersion(prefix: String) {

    assertRace {
      goal = "http://${prefix}speedrunslive.com/tools/oot-bingo-v4/?seed=273307"
      date = date(2011, 10, 30)
    }.isBingo()
  }

  @Test
  internal fun isBingoWhenGithubIoUrl() {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo()
  }

  @Test
  internal fun isBingoWhenJpBetaUrl() {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=424242&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo()
  }

  @ParameterizedTest
  @ValueSource(strings = ["0.9.6.2", "0.9.5.0-j", "0.9.7.0-j"])
  internal fun noBingoWhenOtherBeta(beta: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/beta$beta/bingo.html?seed=860838&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInSrlGoal1(word: String) {

    assertRace {
      goal = "http://speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal $word"
      date = date(2018, 1, 1)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInSrlGoal2(word: String) {

    assertRace {
      goal = "$word http://speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal"
      date = date(2018, 1, 1)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInSrlUrl(word: String) {

    assertRace {
      goal = "http://speedrunslive.com/tools/oot-bingo/?seed=257318&mode=$word"
      date = date(2018, 1, 1)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInGithubIoGoal1(word: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal $word"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInGithubIoGoal2(word: String) {

    assertRace {
      goal = "$word https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInGithubIoUrl(word: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=$word"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["SHORT", "loNG", "BLACKout", "Japanese", "bInGo-J"])
  internal fun noBingoWhenBlacklistedWordInGithubIoGoalCapitalization(word: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=$word"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInBetaGoal1(word: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=normal $word"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInBetaGoal2(word: String) {

    assertRace {
      goal = "$word https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInBetaUrl(word: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=$word"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @ParameterizedTest
  @ValueSource(strings = ["SHORT", "loNG", "BLACKout", "Japanese", "bInGo-J"])
  internal fun noBingoWhenBlacklistedWordInBetaGoalCapitalization(word: String) {

    assertRace {
      goal = "https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=$word"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @Test
  internal fun noBingoWhenRaceIdBlacklisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.blacklist = listOf(raceId)

    assertRace {
      id = raceId
      goal = "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

  @Test
  internal fun isBingoWhenRaceIdWhitelisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    assertRace {
      id = raceId
      goal = "Definitely and totally not a bigno!!!"
      date = date(2019, 10, 27)
    }.isBingo()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun isBingoWhenRaceIdWhitelistedAndGoalContainsBlacklistedWords(word: String) {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    assertRace {
      id = raceId
      goal = "goal $word"
      date = date(2019, 10, 27)
    }.isBingo()
  }

  @Test
  internal fun noBingoWhenRaceIdBlacklistedAndWhitelisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.blacklist = listOf(raceId)
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    assertRace {
      id = raceId
      goal = "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal"
      date = date(2019, 10, 27)
    }.isBingo(false)
  }

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
