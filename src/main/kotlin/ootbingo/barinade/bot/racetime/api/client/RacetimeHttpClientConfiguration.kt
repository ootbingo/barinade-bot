package ootbingo.barinade.bot.racetime.api.client

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.Instant

@Configuration
class RacetimeHttpClientConfiguration {

  @Bean
  fun racetimeRestTemplate(): RestTemplate = RestTemplateBuilder()
      .messageConverters(GsonHttpMessageConverter(racetimeGson()))
      .build()

  @Bean
  fun racetimeGson(): Gson = GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Duration::class.java, durationDeserializer)
      .registerTypeAdapter(Instant::class.java, instantDeserializer)
      .create()

  private val durationDeserializer = JsonDeserializer { json, _, _ ->
    Duration.parse(json.asString)
  }

  private val instantDeserializer = JsonDeserializer { json, _, _ ->
    Instant.parse(json.asString)
  }
}
