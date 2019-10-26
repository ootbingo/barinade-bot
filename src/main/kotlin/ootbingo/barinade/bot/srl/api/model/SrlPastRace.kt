package ootbingo.barinade.bot.srl.api.model

import java.time.ZonedDateTime

data class SrlPastRace(var id: String = "", var game: SrlGame = SrlGame(), var goal: String = "",
                       var date: ZonedDateTime = ZonedDateTime.now(), var numentrants: Long = 0,
                       var results: List<SrlResult> = emptyList())
