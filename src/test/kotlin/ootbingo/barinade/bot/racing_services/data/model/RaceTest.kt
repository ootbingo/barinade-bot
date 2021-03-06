package ootbingo.barinade.bot.racing_services.data.model

import ootbingo.barinade.bot.properties.BingoRaceProperties
import ootbingo.barinade.bot.properties.model.WhitelistBingo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

internal class RaceTest {

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun isBingoWhenSrlUrl(prefix: String) {

    val race = race("http://${prefix}speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal",
                    date(2018, 1, 1))

    assertThat(race.isBingo()).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun noBingoWhenSrlUrlAfterSwitch(prefix: String) {

    val race = race("http://${prefix}speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal",
                    date(2019, 9, 21))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun isBingoWhenSrlUrlWithVersion(prefix: String) {

    val race = race("http://${prefix}speedrunslive.com/tools/oot-bingo-v4/?seed=273307",
                    date(2011, 10, 30))

    assertThat(race.isBingo()).isTrue()
  }

  @Test
  internal fun isBingoWhenGithubIoUrl() {

    val race = race("https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isTrue()
  }

  @Test
  internal fun isBingoWhenJpBetaUrl() {

    val race = race("https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=424242&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["0.9.6.2", "0.9.5.0-j", "0.9.7.0-j"])
  internal fun noBingoWhenOtherBeta(beta: String) {

    val race = race("https://ootbingo.github.io/bingo/beta$beta/bingo.html?seed=860838&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInSrlGoal1(word: String) {

    val race = race("http://speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal $word",
                    date(2018, 1, 1))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInSrlGoal2(word: String) {

    val race = race("$word http://speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal",
                    date(2018, 1, 1))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInSrlUrl(word: String) {

    val race = race("http://speedrunslive.com/tools/oot-bingo/?seed=257318&mode=$word",
                    date(2018, 1, 1))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInGithubIoGoal1(word: String) {

    val race = race("https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal $word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInGithubIoGoal2(word: String) {

    val race = race("$word https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInGithubIoUrl(word: String) {

    val race = race("https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=$word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["SHORT", "loNG", "BLACKout", "Japanese", "bInGo-J"])
  internal fun noBingoWhenBlacklistedWordInGithubIoGoalCapitalization(word: String) {

    val race = race("https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=$word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInBetaGoal1(word: String) {

    val race = race("https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=normal $word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInBetaGoal2(word: String) {

    val race = race("$word https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenBlacklistedWordInBetaUrl(word: String) {

    val race = race("https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=$word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["SHORT", "loNG", "BLACKout", "Japanese", "bInGo-J"])
  internal fun noBingoWhenBlacklistedWordInBetaGoalCapitalization(word: String) {

    val race = race("https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=860838&mode=$word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @Test
  internal fun noBingoWhenRaceIdBlacklisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.blacklist = listOf(raceId)

    val race = race(raceId,
                    "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  @Test
  internal fun isBingoWhenRaceIdWhitelisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    val race = race(raceId,
                    "Definitely and totally not a bigno!!!",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun isBingoWhenRaceIdWhitelistedAndGoalContainsBlacklistedWords(word: String) {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    val race = race(raceId,
                    "goal $word",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isTrue()
  }

  @Test
  internal fun noBingoWhenRaceIdBlacklistedAndWhitelisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.blacklist = listOf(raceId)
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    val race = race(raceId,
                    "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal",
                    date(2019, 10, 27))

    assertThat(race.isBingo()).isFalse()
  }

  private fun race(goal: String, recordDate: Instant): Race =
      Race("0", goal, recordDate, Platform.SRL, mutableListOf())

  private fun race(id: String, goal: String, recordDate: Instant): Race =
      Race(id, goal, recordDate, Platform.SRL, mutableListOf())

  private fun date(year: Int, month: Int, day: Int) =
      ZonedDateTime.of(year, month, day, 1, 1, 1, 0, ZoneId.of("UTC")).toInstant()
}
