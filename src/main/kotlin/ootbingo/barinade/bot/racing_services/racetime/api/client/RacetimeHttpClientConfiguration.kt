package ootbingo.barinade.bot.racing_services.racetime.api.client

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class RacetimeHttpClientConfiguration {

  @Bean
  fun racetimeRestTemplate(): RestTemplate = RestTemplateBuilder()
      .messageConverters(KotlinSerializationJsonHttpMessageConverter(racetimeJson()), StringHttpMessageConverter())
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
