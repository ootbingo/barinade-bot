package ootbingo.barinade.bot.srl.api.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ootbingo.barinade.bot.srl.api.client.deserialize.DateTimeDeserializer
import java.time.ZonedDateTime

@JsonAutoDetect
data class SrlPastRace(var id: String = "", var game: SrlGame = SrlGame(), var goal: String = "",
                       @set:JsonDeserialize(using = DateTimeDeserializer::class)
                       var date: ZonedDateTime = ZonedDateTime.now(),
                       var numentrants: Long = 0,
                       var results: List<SrlResult> = emptyList())
