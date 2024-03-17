package ootbingo.barinade.bot.racing_services.racetime.api.client

import kotlinx.serialization.encodeToString
import ootbingo.barinade.bot.misc.http_converters.urlencoded.SnakeCaseStrategy
import ootbingo.barinade.bot.misc.http_converters.urlencoded.WriteOnlyUrlEncodedHttpMessageConverter
import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.newBingoRace
import ootbingo.barinade.bot.racing_services.racetime.racing.oauth.OAuthManager
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URI
import java.util.*

@ExtendWith(SpringExtension::class)
internal class RacetimeHttpClientTest {

  private val dataBaseUrl = "http://example.com"
  private val racingBaseUrl = "http://example.de"

  private val restTemplate = RacetimeHttpClientConfiguration(mock()).racetimeRestTemplate()
  private val properties: RacetimeApiProperties = RacetimeApiProperties(dataBaseUrl, racingBaseUrl)
  private val oAuthManagerMock: OAuthManager = mock()
  private val server: MockRestServiceServer = MockRestServiceServer.createServer(restTemplate)
  private val client: RacetimeHttpClient = RacetimeHttpClient(restTemplate, properties, oAuthManagerMock)

  @Test
  internal fun findsAllRacesOfGame() {

    fun random() = UUID.randomUUID().toString()

    fun raceWithId(id: String) =
      """
          {
      "name": "$id",
      "status": {
        "value": "finished"
      },
      "goal": {
        "name": "Bingo",
        "custom": false
      },
      "info": "https://ootbingo.github.io/bingo/beta0.9.6.3-j/bingo.html?seed=105340&;amp;mode=normal",
      "entrants_count": 6,
      "entrants": [
        {
          "user": {
            "id": "OR6ym83myb3Pd1Xr",
            "full_name": "Titou#0711",
            "name": "Titou",
            "discriminator": "0711"
          },
          "status": {
            "value": "done"
          },
          "finish_time": "P0DT01H10M47.685904S",
          "place": 1
        },
        {
          "user": {
            "id": "NqO2YoLDL8o9QEya",
            "full_name": "Jake Wright#7726",
            "name": "Jake Wright",
            "discriminator": "7726"
          },
          "status": {
            "value": "done"
          },
          "finish_time": "P0DT01H14M59.350105S",
          "place": 2
        }
      ],
      "ended_at": "2020-07-28T16:21:38.324Z",
      "recordable": true,
      "recorded": true
    }
        """.trimIndent()

    fun categoryRacesJson(vararg raceIds: String) =
      """
          {
            "count": 238,
            "num_pages": 24,
            "races": [
              ${raceIds.joinToString(",") { raceWithId(it) }}
            ]
          }
        """.trimIndent()

    server
      .expect(requestTo("$dataBaseUrl/oot/races/data?show_entrants=true&page=1"))
      .andRespond(withSuccess(categoryRacesJson(random(), random()), MediaType.APPLICATION_JSON))

    server
      .expect(requestTo("$dataBaseUrl/oot/races/data?show_entrants=true&page=2"))
      .andRespond(
        withSuccess(
          categoryRacesJson(random(), random(), random()),
          MediaType.APPLICATION_JSON
        )
      )

    server
      .expect(requestTo("$dataBaseUrl/oot/races/data?show_entrants=true&page=3"))
      .andRespond(withSuccess(categoryRacesJson(random(), random()), MediaType.APPLICATION_JSON))

    server
      .expect(requestTo("$dataBaseUrl/oot/races/data?show_entrants=true&page=4"))
      .andRespond(withSuccess(categoryRacesJson(), MediaType.APPLICATION_JSON))

    val allRaces = client.getAllRaces()

    assertThat(allRaces).hasSize(7)
  }

  @Test
  internal fun findsCurrentRacesOfGame() {

    val categoryJson = """
      {
        "current_races": [
          {
            "name": "oot/superb-spaceman-5763",
            "status": {
              "value": "open"
            },
            "goal": {
              "name": "Bingo",
              "custom": false
            },
            "info": ""
          },
          {
            "name": "oot/jolly-rogers-5763",
            "status": {
              "value": "open"
            },
            "goal": {
              "name": "Bingo",
              "custom": false
            },
            "info": ""
          }
        ]
      }
    """.trimIndent()

    server
      .expect(requestTo("$racingBaseUrl/oot/data"))
      .andRespond(withSuccess(categoryJson, MediaType.APPLICATION_JSON))

    val allRaces = client.getOpenRaces()

    assertThat(allRaces).hasSize(2)
  }

  @Test
  internal fun createsRace() {

    val converter = WriteOnlyUrlEncodedHttpMessageConverter(SnakeCaseStrategy)

    val (info, token, responseUrlString) = (1..3).map { UUID.randomUUID().toString() }
    val race = newBingoRace(false).apply { infoBot = info }

    val responseUrl = URI("/$responseUrlString")

    whenever(oAuthManagerMock.getToken()).thenReturn(token)

    server
      .expect(requestTo("$racingBaseUrl/o/oot/startrace"))
      .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
      .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
      .andExpect(content().string(convert(converter, race)))
      .andExpect(method(HttpMethod.POST))
      .andRespond(withCreatedEntity(responseUrl))

    val raceUrl = client.startRace(race)

    assertThat(raceUrl).isEqualTo("$racingBaseUrl$responseUrl")
  }

  @Test
  internal fun editsRace() {

    val json = RacetimeHttpClientConfiguration(mock()).racetimeJson()
    val converter = WriteOnlyUrlEncodedHttpMessageConverter(SnakeCaseStrategy)

    val (raceSlug, newInfoBot, token) = (1..3).map { UUID.randomUUID().toString() }

    val race = RacetimeRace(
      name = raceSlug,
    )

    val raceJson = json.encodeToString(race)

    whenever(oAuthManagerMock.getToken()).thenReturn(token)

    server
      .expect(requestTo("$racingBaseUrl/oot/$raceSlug/data"))
      .andRespond(withSuccess(raceJson, MediaType.APPLICATION_JSON))

    server
      .expect(requestTo("$racingBaseUrl/o/oot/$raceSlug/edit"))
      .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
      .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
      .andRespond(withSuccess(raceJson, MediaType.APPLICATION_JSON))

    val edit: RacetimeEditableRace.() -> Unit = {
      infoBot = newInfoBot
      autoStart = true
    }

    client.editRace(raceSlug, edit)

    convert(converter, race.toEditableRace().apply(edit))
  }

  private fun <T : Any> convert(converter: HttpMessageConverter<T>, t: T): String {

    val inputStream = PipedInputStream()
    val outputStream = PipedOutputStream(inputStream)

    converter.write(t, MediaType.APPLICATION_FORM_URLENCODED, object : HttpOutputMessage {
      override fun getHeaders(): HttpHeaders = mock()
      override fun getBody(): OutputStream = outputStream
    })

    return inputStream.bufferedReader().use { it.readText() }
  }
}
