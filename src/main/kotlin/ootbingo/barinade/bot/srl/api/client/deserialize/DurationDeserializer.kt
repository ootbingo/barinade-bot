package ootbingo.barinade.bot.srl.api.client.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Duration

class DurationDeserializer: JsonDeserializer<Duration>() {

  override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): Duration {

    return Duration.ofSeconds(requireNotNull(parser?.text?.toLong()))
  }
}
