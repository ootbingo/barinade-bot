package ootbingo.barinade.bot.racing_services.racetime.api.client

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
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
      .registerTypeAdapter(RacetimeEntrant.RacetimeEntrantStatus::class.java, entrantStatusDeserializer)
      .registerTypeAdapter(RacetimeRace.RacetimeRaceStatus::class.java, raceStatusDeserializer)
      .create()

  private val durationDeserializer = JsonDeserializer { json, _, _ ->
    Duration.parse(json.asString)
  }

  private val instantDeserializer = JsonDeserializer { json, _, _ ->
    Instant.parse(json.asString)
  }

  private val entrantStatusDeserializer = JsonDeserializer { json, _, _ ->
    RacetimeEntrant.RacetimeEntrantStatus
        .values()
        .find { it.name.equals(json.asJsonObject.get("value").asString, true) }
  }

  private val raceStatusDeserializer = JsonDeserializer { json, _, _ ->
    RacetimeRace.RacetimeRaceStatus
        .values()
        .find { it.name.equals(json.asJsonObject.get("value").asString, true) }
  }
}
