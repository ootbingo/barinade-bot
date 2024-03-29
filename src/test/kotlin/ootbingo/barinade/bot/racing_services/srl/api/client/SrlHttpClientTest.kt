package ootbingo.barinade.bot.racing_services.srl.api.client

import ootbingo.barinade.bot.racing_services.srl.api.SrlApiProperties
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
internal class SrlHttpClientTest {

  private val baseUrl = "http://example.org"

  private var properties: SrlApiProperties = SrlApiProperties(baseUrl)
  private val restTemplate = RestTemplate()
  private var server: MockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
  private var client: SrlHttpClient = SrlHttpClient(properties, restTemplate)

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
        .expect(requestTo("$baseUrl/players/$playerName"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    val player = requireNotNull(client.getPlayerByName(playerName))

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
        .expect(requestTo("$baseUrl/players/$playerName"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    assertThat(client.getPlayerByName(playerName)).isNull()
  }

  @Test
  internal fun getsNullIfPlayerUnknown() {

    val playerName = UUID.randomUUID().toString()

    server
        .expect(requestTo("$baseUrl/players/$playerName"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND))

    assertThat(client.getPlayerByName(playerName)).isNull()
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
        .expect(requestTo("$baseUrl/games/$abbreviation"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    val game = requireNotNull(client.getGameByAbbreviation(abbreviation))

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

    server
        .expect(requestTo("$baseUrl/games/$abbreviation"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND))

    assertThat(client.getGameByAbbreviation(abbreviation)).isNull()
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
        .expect(requestTo("$baseUrl/pastraces?player=$playerName&pageSize=2000"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON))

    val races = requireNotNull(client.getRacesByPlayerName(playerName))

    val soft = SoftAssertions()

    assertThat(races).hasSize(1)

    val race = races[0]
    soft.assertThat(race.id).isEqualTo(raceId.toString())
    soft.assertThat(race.game.id).isEqualTo(gameId)
    soft.assertThat(race.game.name).isEqualTo(gameName)
    soft.assertThat(race.game.abbrev).isEqualTo(abbrev)
    soft.assertThat(race.goal).isEqualTo(goal)
    soft.assertThat(race.date).isEqualTo(Instant.ofEpochSecond(date))

    assertThat(race.results).hasSize(1)

    val result = race.results[0]
    soft.assertThat(result.race).isEqualTo(raceId)
    soft.assertThat(result.place).isEqualTo(place)
    soft.assertThat(result.player).isEqualTo(playerName)
    soft.assertThat(result.time).isEqualTo(Duration.ofSeconds(time))
    soft.assertThat(result.message).isEqualTo(message)

    soft.assertAll()
  }

  @Test
  internal fun findsAllRacesOfGame() {

    fun jsonWithPlayers(player1: String, player2: String) =
        """
      {
        "count" : 5326,
        "pastraces" :
          [
            { "id" : "${Random.nextInt(0, 5000)}",
              "game":{ "id":1, "name":"Ocarina of Time","abbrev":"oot","popularity":1,"popularityrank":1},
              "goal":"goal","date":"${Random.nextLong(0, 1572119797)}","numentrants":1,
              "results" :
                [
                  {
                    "race":45,"place":1,"player":"$player1","time":45,"message":"hi",
                    "oldtrueskill" : 0,"newtrueskill" : 0,"trueskillchange" : 0,
                    "oldseasontrueskill" : 0,"newseasontrueskill" : 0,"seasontrueskillchange" : 0
                  },
                  {
                    "race":45,"place":2,"player":"$player2","time":46,"message":"hi",
                    "oldtrueskill" : 0,"newtrueskill" : 0,"trueskillchange" : 0,
                    "oldseasontrueskill" : 0,"newseasontrueskill" : 0,"seasontrueskillchange" : 0
                  }
                ]
            }
          ]
      }
    """.trimIndent()

    val emptyJson = """
      {
      "count" : 5326,
      "pastraces" :
      [
      ]
      }
    """.trimIndent()

    server
        .expect(requestTo("$baseUrl/pastraces?game=oot&pageSize=2000&page=1"))
        .andRespond(withSuccess(jsonWithPlayers("Konrad", "Ludwig"), MediaType.APPLICATION_JSON))

    server
        .expect(requestTo("$baseUrl/pastraces?game=oot&pageSize=2000&page=2"))
        .andRespond(withSuccess(jsonWithPlayers("Ludwig", "Gerhard"), MediaType.APPLICATION_JSON))

    server
        .expect(requestTo("$baseUrl/pastraces?game=oot&pageSize=2000&page=3"))
        .andRespond(withSuccess(jsonWithPlayers("Willy", "Helmut"), MediaType.APPLICATION_JSON))

    server
        .expect(requestTo("$baseUrl/pastraces?game=oot&pageSize=2000&page=4"))
        .andRespond(withSuccess(emptyJson, MediaType.APPLICATION_JSON))

    val allRaces = client.getAllRacesOfGame("oot")
    val abstractRaces =
        allRaces.map { it.results }
            .map { it.map { r -> r.player to r.place } }
            .map { it[0] to it[1] }

    assertThat(abstractRaces).containsExactlyInAnyOrder((("Konrad" to 1L) to ("Ludwig" to 2L)),
        (("Ludwig" to 1L) to ("Gerhard" to 2L)),
        (("Willy" to 1L) to ("Helmut" to 2L)))
  }
}
