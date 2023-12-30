package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

fun interface WebsocketConnector {

  fun connect(url: String, delegate: RaceConnection): RaceWebsocketHandler
}
