package ootbingo.barinade.bot.racetime.api.client

import ootbingo.barinade.bot.racetime.api.RacetimeApiProperties
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators
import java.util.UUID

@ExtendWith(SpringExtension::class)
@RestClientTest(components = [RacetimeHttpClient::class, RacetimeApiProperties::class])
@AutoConfigureWebClient(registerRestTemplate = true)
internal class RacetimeHttpClientTest {

  private val baseUrl = "http://example.com"

  @Autowired
  private var properties: RacetimeApiProperties? = null

  @Autowired
  private var server: MockRestServiceServer? = null

  @Autowired
  private var client: RacetimeHttpClient? = null

  @BeforeEach
  internal fun setup() {
    requireNotNull(properties).baseUrl = baseUrl
  }

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
        ?.expect(requestTo("$baseUrl/oot/races/data?show_entrants=true&page=1"))
        ?.andRespond(
            MockRestResponseCreators.withSuccess(categoryRacesJson(random(), random()), MediaType.APPLICATION_JSON))

    server
        ?.expect(requestTo("$baseUrl/oot/races/data?show_entrants=true&page=2"))
        ?.andRespond(
            MockRestResponseCreators.withSuccess(categoryRacesJson(random(), random(), random()),
                                                 MediaType.APPLICATION_JSON))

    server
        ?.expect(requestTo("$baseUrl/oot/races/data?show_entrants=true&page=3"))
        ?.andRespond(
            MockRestResponseCreators.withSuccess(categoryRacesJson(random(), random()), MediaType.APPLICATION_JSON))

    server
        ?.expect(requestTo("$baseUrl/oot/races/data?show_entrants=true&page=4"))
        ?.andRespond(MockRestResponseCreators.withSuccess(categoryRacesJson(), MediaType.APPLICATION_JSON))

    val allRaces = client?.getAllRacesOfGame("oot") ?: emptySet()

    assertThat(allRaces).hasSize(7)
  }
}
