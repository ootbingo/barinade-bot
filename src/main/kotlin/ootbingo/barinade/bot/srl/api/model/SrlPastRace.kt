package ootbingo.barinade.bot.srl.api.model

import java.time.ZonedDateTime

data class SrlPastRace(val id: String, val game: SrlGame, val goal: String, val date: ZonedDateTime, val numentrants: Long,
                       val results: List<SrlResult>)
