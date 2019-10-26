package ootbingo.barinade.bot.srl.api.client

import ootbingo.barinade.bot.srl.api.SrlApiProperties
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import java.net.URI
import java.util.UUID
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@RestClientTest(components = [SrlHttpClient::class, SrlApiProperties::class])
@AutoConfigureWebClient(registerRestTemplate = true)
internal class SrlHttpClientTest {

  private val baseUrl = "http://example.org"
  private val playerName = UUID.randomUUID().toString()
  private val playerId = Random.nextLong()
  private val api = UUID.randomUUID().toString()
  private val country = UUID.randomUUID().toString()

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
  internal fun getsGame() {
    Assertions.fail<Unit>("Test case 'getsGame' not implemented.")
  }

  @Test
  internal fun getRacesByPlayer() {
    Assertions.fail<Unit>("Test case 'getRacesByPlayer' not implemented.")
  }
}