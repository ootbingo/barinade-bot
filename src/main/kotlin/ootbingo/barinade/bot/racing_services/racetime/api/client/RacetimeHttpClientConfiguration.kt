package ootbingo.barinade.bot.racing_services.racetime.api.client

import com.google.gson.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeAction
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.SendMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.SetGoal
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
      .registerTypeAdapter(RacetimeAction::class.java, actionDeserializer)
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

  private val actionDeserializer: JsonDeserializer<RacetimeAction> = JsonDeserializer { json, _, _ ->

    val action = json.asJsonObject["action"].asString
    val data = json.asJsonObject["data"].asJsonObject

    val payload = when (action) {
      "message" -> SendMessage(data["message"].asString, data["guid"].asString)
      "setinfo" -> SetGoal(data["info"].asString)
      else -> throw JsonParseException("Cannot parse ${json.asString}")
    }

    RacetimeAction(action, payload)
  }
}
