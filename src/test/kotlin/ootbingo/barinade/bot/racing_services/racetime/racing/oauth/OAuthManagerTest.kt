package ootbingo.barinade.bot.racing_services.racetime.racing.oauth

import com.nhaarman.mockitokotlin2.*
import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

internal class OAuthManagerTest {

  //<editor-fold desc="Setup">

  private val token = AtomicReference(Token())
  private val properties = RacetimeApiProperties()
  private val restTemplateMock = mock<RestTemplate>()
  private var currentTime: Instant = Instant.now()
  private val manager = OAuthManager(token, properties, restTemplateMock) { currentTime }

  private lateinit var thenTokenReturned: String
  private val thenExpiryTime: Instant
    get() = token.get().expiryTime

  //</editor-fold>

  @Test
  internal fun returnsSavedTokenIfStillValid() {

    val tokenString = UUID.randomUUID().toString()

    givenToken {
      bears(tokenString)
      expiresIn(5, HOURS)
    }

    whenTokenIsRequested()

    thenTokenReturned isEqualTo tokenString
  }

  @Test
  internal fun returnsRenewedTokenWhenValidForLessThanTwoHours() {

    givenToken {
      bears(UUID.randomUUID().toString())
      expiresIn(119, MINUTES)
    }

    returnsRenewedToken()
  }

  @Test
  internal fun returnsRenewedTokenWhenNotInitialized() {

    returnsRenewedToken()
  }

  private fun returnsRenewedToken() {

    val baseUrl = "https://example${Random.nextInt()}.org"
    givenBaseUrl(baseUrl)

    val clientId = UUID.randomUUID().toString()
    val clientSecret = UUID.randomUUID().toString()
    givenClientCredentials(clientId, clientSecret)

    val newToken = UUID.randomUUID().toString()
    val expiresInSeconds = Random.nextLong(7201, 72000)
    givenTokenReturned(newToken, expiresInSeconds) { whenTokenIsRequested() }

    thenTokenReturned isEqualTo newToken
    thenExpiryTime isEqualTo currentTime.plusSeconds(expiresInSeconds)
  }

  //<editor-fold desc="Given">

  private fun givenToken(block: TokenManipulation.() -> Unit) {
    val manipulation = TokenManipulation().apply(block)
    token.set(Token(token = manipulation.payload, expiryTime = currentTime.plus(manipulation.expiryTime)))
  }

  private fun givenBaseUrl(url: String) {
    properties.racingBaseUrl = url
  }

  private fun givenClientCredentials(clientId: String, clientSecret: String) {
    properties.oauth = RacetimeApiProperties.RacetimeOAuthProperties(clientId, clientSecret)
  }

  private fun givenTokenReturned(newToken: String, expiresInSeconds: Long, testInvocation: () -> Unit) {

    fun String.getFormParameter(parameterName: String): String =
        split("&")
            .map { part -> part.split("=").let { it[0] to it[1] } }
            .filter { it.first == parameterName }
            .takeIf { it.size == 1 }
            ?.last()?.second ?: fail("Form Parameter '$parameterName' missing")


    val formDataCaptor = argumentCaptor<HttpEntity<String>>()

    whenever(
        restTemplateMock.postForEntity(any<String>(),
            any<HttpEntity<String>>(),
            eq(OAuthManager.TokenResponse::class.java))
    ).thenReturn(ResponseEntity.ok(OAuthManager.TokenResponse(newToken, expiresInSeconds)))

    testInvocation()

    verify(restTemplateMock).postForEntity(eq("${properties.racingBaseUrl}/o/token"),
        formDataCaptor.capture(),
        eq(OAuthManager.TokenResponse::class.java))
    verifyNoMoreInteractions(restTemplateMock)

    val formDataEntity = formDataCaptor.lastValue
    val formString = formDataEntity.body!!

    assertThat(formDataEntity.headers[HttpHeaders.CONTENT_TYPE])
        .containsExactly(MediaType.APPLICATION_FORM_URLENCODED.toString())
    assertThat(formString.getFormParameter("client_id")).isEqualTo(properties.oauth.clientId)
    assertThat(formString.getFormParameter("client_secret")).isEqualTo(properties.oauth.clientSecret)
    assertThat(formString.getFormParameter("grant_type")).isEqualTo("client_credentials")
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenTokenIsRequested() {
    thenTokenReturned = manager.getToken()
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private infix fun <T> T.isEqualTo(other: T) =
      assertThat(this).isEqualTo(other)

  //</editor-fold>

  //<editor-fold desc="Helper">

  private class TokenManipulation {
    var payload: String = ""
      private set
    var expiryTime: TemporalAmount = Duration.ZERO
      private set

    fun bears(payload: String) {
      this.payload = payload
    }

    fun expiresIn(amount: Long, unit: TemporalUnit) {
      expiryTime = Duration.of(amount, unit)
    }
  }

  //</editor-fold>
}
