package ootbingo.barinade.bot.racing_services.racetime.api.client

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
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.client.RestTemplate
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
}
