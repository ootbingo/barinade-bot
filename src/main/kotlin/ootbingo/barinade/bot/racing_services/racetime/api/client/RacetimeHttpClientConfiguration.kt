package ootbingo.barinade.bot.racing_services.racetime.api.client

import com.google.gson.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeAction
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeSurveyType
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.SendMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.SetGoal
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

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

  abstract class RacetimeEnumSerializer<T : Enum<T>>(
      private val type: KClass<T>,
  ) : KSerializer<T> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(checkNotNull(type.simpleName)) {
      element<String>("value")
    }

    override fun serialize(encoder: Encoder, value: T) =
        encoder.encodeStructure(descriptor) {
          encodeStringElement(descriptor, 0, value.name.lowercase())
        }

    override fun deserialize(decoder: Decoder): T =
        decoder.decodeStructure(descriptor) {

          var value = ""

          while (true) {
            when (val index = decodeElementIndex(descriptor)) {
              0 -> value = decodeStringElement(descriptor, 0)
              CompositeDecoder.DECODE_DONE -> break
              else -> throw IndexOutOfBoundsException("Unexpected index $index")
            }
          }

          value.let {
            type.java.enumConstants
                .find { c -> c.name.equals(it, true) }
                ?: throw IllegalArgumentException("Value '$it' not valid for type ${type.simpleName}")
          }
        }
  }

  class EntrantStatusSerializer : RacetimeEnumSerializer<RacetimeEntrant.RacetimeEntrantStatus>(RacetimeEntrant.RacetimeEntrantStatus::class)
  class RaceStatusSerializer : RacetimeEnumSerializer<RacetimeRace.RacetimeRaceStatus>(RacetimeRace.RacetimeRaceStatus::class)

  //<editor-fold desc="GSON converted">

  @Bean
  fun racetimeGson(): Gson = GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Duration::class.java, durationDeserializer)
      .registerTypeAdapter(Instant::class.java, instantDeserializer)
      .registerTypeAdapter(Instant::class.java, instantSerializer)
      .registerTypeAdapter(RacetimeSurveyType::class.java, surveyTypeSerializer)
      .registerTypeAdapter(RacetimeEntrant.RacetimeEntrantStatus::class.java, entrantStatusDeserializer)
      .registerTypeAdapter(RacetimeRace.RacetimeRaceStatus::class.java, raceStatusDeserializer)
      .registerTypeAdapter(RacetimeRace.RacetimeRaceStatus::class.java, raceStatusSerializer)
      .registerTypeAdapter(RacetimeAction::class.java, actionDeserializer)
      .create()

  private val durationDeserializer = JsonDeserializer { json, _, _ ->
    Duration.parse(json.asString)
  }

  private val instantDeserializer = JsonDeserializer { json, _, _ ->
    Instant.parse(json.asString)
  }

  private val instantSerializer = JsonSerializer<Instant> { instant, _, _ ->
    JsonPrimitive(instant.toString())
  }

  private val entrantStatusDeserializer = JsonDeserializer { json, _, _ ->
    RacetimeEntrant.RacetimeEntrantStatus
        .entries
        .find { it.name.equals(json.asJsonObject.get("value").asString, true) }
  }

  private val raceStatusDeserializer = JsonDeserializer { json, _, _ ->
    RacetimeRace.RacetimeRaceStatus
        .entries
        .find { it.name.equals(json.asJsonObject.get("value").asString, true) }
  }

  private val raceStatusSerializer = JsonSerializer<RacetimeRace.RacetimeRaceStatus> { status, _, _ ->
    JsonObject().apply { addProperty("value", status.name) }
  }

  private val surveyTypeSerializer = JsonSerializer<RacetimeSurveyType> { type, _, _ ->
    JsonPrimitive(type.name.lowercase())
  }

  private val actionDeserializer: JsonDeserializer<RacetimeAction> = JsonDeserializer { json, _, _ ->

    val action = json.asJsonObject["action"].asString
    val data = json.asJsonObject["data"].asJsonObject

    val payload = when (action) {
      "message" -> SendMessage(data["message"].asString, guid = data["guid"].asString)
      "setinfo" -> SetGoal(data["info"].asString)
      else -> throw JsonParseException("Cannot parse ${json.asString}")
    }

    RacetimeAction(action, payload)
  }

  //</editor-fold>
}
