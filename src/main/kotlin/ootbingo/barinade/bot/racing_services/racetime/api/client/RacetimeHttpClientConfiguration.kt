package ootbingo.barinade.bot.racing_services.racetime.api.client

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import ootbingo.barinade.bot.misc.VersionProperties
import ootbingo.barinade.bot.misc.http_converters.urlencoded.SnakeCaseStrategy
import ootbingo.barinade.bot.misc.http_converters.urlencoded.WriteOnlyUrlEncodedHttpMessageConverter
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class RacetimeHttpClientConfiguration(private val versionProperties: VersionProperties) {

  @Bean
  fun racetimeRestTemplate(): RestTemplate = RestTemplateBuilder()
      .defaultHeader(
          HttpHeaders.USER_AGENT,
          "BingoBot/${versionProperties.version} (${versionProperties.build})"
      )
      .messageConverters(
          KotlinSerializationJsonHttpMessageConverter(racetimeJson()),
          StringHttpMessageConverter(),
          WriteOnlyUrlEncodedHttpMessageConverter(SnakeCaseStrategy),
      )
      .build()

  @Bean
  @OptIn(ExperimentalSerializationApi::class)
  fun racetimeJson() = Json {
    namingStrategy = JsonNamingStrategy.SnakeCase
    decodeEnumsCaseInsensitive = true
    ignoreUnknownKeys = true
    encodeDefaults = true
  }
}
