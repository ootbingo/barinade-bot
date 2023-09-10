package ootbingo.barinade.bot.racing_services.racetime.racing.oauth

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@Component
class OAuthManager(
    private val token: AtomicReference<Token> = AtomicReference(Token()),
    private val properties: RacetimeApiProperties,
    private val racetimeRestTemplate: RestTemplate,
    private val timeSupplier: () -> Instant,
) {

  fun getToken(): String =
      token.get()
          .takeUnless { it.expired(timeSupplier()) }
          ?.token
          ?: renewToken()

  private fun renewToken(): String {

    val request = properties.oauth.let { TokenRequest(it.clientId, it.clientSecret, "client_credentials") }

    val tokenResponse = racetimeRestTemplate.postForEntity(
        "${properties.racingBaseUrl}/o/token",
        HttpEntity(request.toFormData(), HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }),
        TokenResponse::class.java
    )

    return tokenResponse
        .body
        ?.also { token.set(it.toToken(timeSupplier())) }
        ?.accessToken
        ?: throw IllegalStateException("Empty token payload returned")
  }

  @Serializable
  internal data class TokenRequest(
      val clientId: String,
      val clientSecret: String,
      val grantType: String = "client_credentials",
  ) {

    fun toFormData() =
        "client_id=$clientId&client_secret=$clientSecret&grant_type=$grantType"
  }

  @Serializable
  internal data class TokenResponse(
      val accessToken: String,
      val expiresIn: Long,
  ) {

    fun toToken(now: Instant) = Token(accessToken, now.plusSeconds(expiresIn))
  }
}
