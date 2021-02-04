package ootbingo.barinade.bot.racing_services.racetime.racing

interface WebsocketConnector {
  fun connect(url: String, delegate: RaceConnection): RaceWebsocketHandler
}
