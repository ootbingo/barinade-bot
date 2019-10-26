package ootbingo.barinade.bot.srl.api.model

import java.time.Duration

data class SrlResult(var race: Long = 0, var place: Long = 0, var player: String = "",
                     var time: Duration = Duration.ZERO, var message: String = "", var oldtrueskill: Long = 0,
                     var newtrueskill: Long = 0, var trueskillchange: Long = 0, var oldseasontrueskill: Long = 0,
                     var newseasontrueskill: Long = 0, var seasontrueskillchange: Long = 0)
