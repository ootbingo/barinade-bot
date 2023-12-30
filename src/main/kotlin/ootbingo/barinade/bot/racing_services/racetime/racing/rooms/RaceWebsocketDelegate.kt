package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

interface RaceWebsocketDelegate {

  val slug: String
  fun onMessage(message: RacetimeMessage)
  fun closeWebsocket()
}
