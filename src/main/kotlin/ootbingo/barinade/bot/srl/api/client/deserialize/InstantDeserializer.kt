package ootbingo.barinade.bot.srl.api.client.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class InstantDeserializer : JsonDeserializer<Instant>() {

  override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): Instant {

    return Instant.ofEpochSecond(requireNotNull(parser?.text?.toLong()))
  }
}
