package ootbingo.barinade.bot.srl.api.client

import ootbingo.barinade.bot.srl.api.SrlApiProperties
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@RestClientTest(components = [SrlHttpClient::class, SrlApiProperties::class])
@AutoConfigureWebClient(registerRestTemplate = true)
internal class SrlHttpClientTest {

  private val baseUrl = "http://example.org"

  @Autowired
  private var properties: SrlApiProperties? = null

  @Autowired
  private var server: MockRestServiceServer? = null

  @Autowired
  private var client: SrlHttpClient? = null

  @BeforeEach
  internal fun setup() {
    requireNotNull(properties).baseUrl = baseUrl
  }

  @Test
  internal fun getsPlayer() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong()
    val api = UUID.randomUUID().toString()
    val country = UUID.randomUUID().toString()

    val json = """
      {
        "id" : $playerId,
        "name" : "$playerName",
        "channel" : "${playerName}channel",
        "api" : "$api",
        "twitter" : "${playerName}twitter",
        "youtube" : "${playerName}youtube",
        "country" : "$country"
      }
    """.trimIndent()

    server
        ?.expect(requestTo("$baseUrl/players/$playerName"))
        ?.andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    val player = requireNotNull(client?.getPlayerByName(playerName))

    val soft = SoftAssertions()

    soft.assertThat(player.id).isEqualTo(playerId)
    soft.assertThat(player.name).isEqualTo(playerName)
    soft.assertThat(player.channel).isEqualTo(playerName + "channel")
    soft.assertThat(player.api).isEqualTo(api)
    soft.assertThat(player.twitter).isEqualTo(playerName + "twitter")
    soft.assertThat(player.youtube).isEqualTo(playerName + "youtube")
    soft.assertThat(player.country).isEqualTo(country)

    soft.assertAll()
  }

  @Test
  internal fun getsNullIfPlayerIdIsZero() {

    val playerName = UUID.randomUUID().toString()

    val json = """
      {
        "id" : 0,
        "name" : "$playerName",
        "channel" : "",
        "api" : "",
        "twitter" : "",
        "youtube" : "",
        "country" : ""
      }
    """.trimIndent()

    server
        ?.expect(requestTo("$baseUrl/players/$playerName"))
        ?.andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    assertThat(client?.getPlayerByName(playerName)).isNull()
  }

  @Test
  internal fun getsNullIfPlayerUnknown() {

    val playerName = UUID.randomUUID().toString()

    val json = """
      {
        "errorCode" : 404,
        "errorText" : "Not Found"
      }
    """.trimIndent()

    server
        ?.expect(requestTo("$baseUrl/players/$playerName"))
        ?.andRespond(withStatus(HttpStatus.NOT_FOUND))

    assertThat(client?.getPlayerByName(playerName)).isNull()
  }

  @Test
  internal fun getsGame() {

    val gameId = Random.nextLong()
    val gameName = UUID.randomUUID().toString()
    val abbreviation = gameName + "abbreviation"
    val popularity = Random.nextDouble()
    val popularityRank = Random.nextLong()

    val json = """
      {
        "id" : $gameId,
        "name" : "$gameName",
        "abbrev" : "$abbreviation",
        "popularity" : $popularity,
        "popularityrank" : $popularityRank
      }
    """.trimIndent()

    server
        ?.expect(requestTo("$baseUrl/games/$abbreviation"))
        ?.andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    val game = requireNotNull(client?.getGameByAbbreviation(abbreviation))

    val soft = SoftAssertions()

    soft.assertThat(game.id).isEqualTo(gameId)
    soft.assertThat(game.name).isEqualTo(gameName)
    soft.assertThat(game.abbrev).isEqualTo(abbreviation)
    soft.assertThat(game.popularity).isEqualTo(popularity)
    soft.assertThat(game.popularityrank).isEqualTo(popularityRank)

    soft.assertAll()
  }

  @Test
  internal fun getsNullIfGameUnknown() {

    val abbreviation = UUID.randomUUID().toString()

    val json = """
      {
        "errorCode" : 404,
        "errorText" : "Not Found"
      }
    """.trimIndent()

    server
        ?.expect(requestTo("$baseUrl/games/$abbreviation"))
        ?.andRespond(withStatus(HttpStatus.NOT_FOUND))

    assertThat(client?.getGameByAbbreviation(abbreviation)).isNull()
  }

  @Test
  internal fun getRacesByPlayer() {

    val raceId = Random.nextLong()
    val gameId = Random.nextLong()
    val playerName = UUID.randomUUID().toString()
    val gameName = UUID.randomUUID().toString()
    val abbrev = UUID.randomUUID().toString()
    val pop = Random.nextDouble()
    val popRank = Random.nextLong()
    val goal = UUID.randomUUID().toString()
    val date = Random.nextLong(0, 1572119797)
    val place = Random.nextLong()
    val time = Random.nextLong(0, 260000)
    val message = UUID.randomUUID().toString()

    val json = """
      {
        "count" : 1,
        "pastraces" :
          [
            { "id" : "$raceId",
              "game":{ "id":$gameId, "name":"$gameName","abbrev":"$abbrev","popularity":$pop,"popularityrank":$popRank},
              "goal":"$goal","date":"$date","numentrants":1,
              "results" :
                [
                  {
                    "race":$raceId,"place":$place,"player":"$playerName","time":$time,"message":"$message",
                    "oldtrueskill" : 0,"newtrueskill" : 0,"trueskillchange" : 0,
                    "oldseasontrueskill" : 0,"newseasontrueskill" : 0,"seasontrueskillchange" : 0
                  }
                ]
            }
          ]
      }
    """.trimIndent()

    server
        ?.expect(requestTo("$baseUrl/pastraces?player=$playerName&pageSize=2000"))
        ?.andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    val races = requireNotNull(client?.getRacesByPlayerName(playerName))

    val soft = SoftAssertions()

    assertThat(races).hasSize(1)

    val race = races[0]
    soft.assertThat(race.id).isEqualTo(raceId.toString())
    soft.assertThat(race.game.id).isEqualTo(gameId)
    soft.assertThat(race.game.name).isEqualTo(gameName)
    soft.assertThat(race.game.abbrev).isEqualTo(abbrev)
    soft.assertThat(race.goal).isEqualTo(goal)
    soft.assertThat(race.date).isEqualTo(ZonedDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneId.of("UTC")))

    assertThat(race.results).hasSize(1)

    val result = race.results[0]
    soft.assertThat(result.race).isEqualTo(raceId)
    soft.assertThat(result.place).isEqualTo(place)
    soft.assertThat(result.player).isEqualTo(playerName)
    soft.assertThat(result.time).isEqualTo(Duration.ofSeconds(time))
    soft.assertThat(result.message).isEqualTo(message)

    soft.assertAll()
  }
}