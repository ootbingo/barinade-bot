package ootbingo.barinade.bot.racing_services.srl.api.client.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant

class InstantDeserializer : JsonDeserializer<Instant>() {

  override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): Instant {

    return Instant.ofEpochSecond(requireNotNull(parser?.text?.toLong()))
  }
}
