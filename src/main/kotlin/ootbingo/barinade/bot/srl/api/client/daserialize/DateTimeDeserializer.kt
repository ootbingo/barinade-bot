package ootbingo.barinade.bot.srl.api.client.daserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class DateTimeDeserializer: JsonDeserializer<ZonedDateTime>() {

  override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): ZonedDateTime {

    println("DateTime of: ${parser?.text}")

    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(requireNotNull(parser?.text?.toLong())), ZoneId.of("UTC"))
  }
}
