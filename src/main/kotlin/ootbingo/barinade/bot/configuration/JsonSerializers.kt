package ootbingo.barinade.bot.configuration

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeSurveyType
import java.time.Instant
import kotlin.reflect.KClass

typealias SerializableInstant = @Serializable(InstantSerializer::class) Instant

object InstantSerializer : KSerializer<Instant> {

  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) =
      encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): Instant =
      Instant.parse(decoder.decodeString())
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

class EntrantStatusSerializer : RacetimeEnumSerializer<RacetimeEntrantStatus>(RacetimeEntrantStatus::class)
class RaceStatusSerializer : RacetimeEnumSerializer<RacetimeRaceStatus>(RacetimeRaceStatus::class)

class RacetimeSurveyTypeSerializer : KSerializer<RacetimeSurveyType> {

  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("RacetimeSurveyTypeSerializer", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): RacetimeSurveyType =
      RacetimeSurveyType.valueOf(decoder.decodeString().uppercase())

  override fun serialize(encoder: Encoder, value: RacetimeSurveyType) =
      encoder.encodeString(value.name.lowercase())
}
