package ootbingo.barinade.bot.srl.api.model

import java.time.Duration

data class SrlResult(val race: Long, val place: Long, val player: String, val time: Duration, val message: String,
                     val oldtrueskill: Long, val newtrueskill: Long, val trueskillchange: Long,
                     val oldseasontrueskill: Long, val newseasontrueskill: Long, val seasontrueskillchange: Long)
