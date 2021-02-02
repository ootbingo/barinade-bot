package ootbingo.barinade.bot.racing_services.racetime.racing

interface RaceConnectionFactory {
  fun openConnection(raceEndpoint: String)
}
