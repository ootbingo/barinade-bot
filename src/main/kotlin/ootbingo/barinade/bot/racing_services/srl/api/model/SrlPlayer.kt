package ootbingo.barinade.bot.racing_services.srl.api.model

data class SrlPlayer(
    var id: Long = 0, var name: String = "", var channel: String = "", var api: String = "",
    var twitter: String = "", var youtube: String = "", var country: String = "",
)
