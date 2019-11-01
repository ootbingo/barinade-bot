package ootbingo.barinade.bot.srl.api.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ootbingo.barinade.bot.srl.api.client.deserialize.DurationDeserializer
import java.time.Duration

data class SrlResult(var race: Long = 0, var place: Long = 0, var player: String = "",
                     @set:JsonDeserialize(using = DurationDeserializer::class)
                     var time: Duration = Duration.ZERO,
                     var message: String = "", var oldtrueskill: Long = 0, var newtrueskill: Long = 0,
                     var trueskillchange: Long = 0, var oldseasontrueskill: Long = 0, var newseasontrueskill: Long = 0,
                     var seasontrueskillchange: Long = 0)
