package ootbingo.barinade.bot.srl.api.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ootbingo.barinade.bot.srl.api.client.deserialize.InstantDeserializer
import java.time.Instant

@JsonAutoDetect
data class SrlPastRace(var id: String = "", var game: SrlGame = SrlGame(), var goal: String = "",
                       @set:JsonDeserialize(using = InstantDeserializer::class)
                       var date: Instant = Instant.now(),
                       var numentrants: Long = 0,
                       var results: List<SrlResult> = emptyList())
