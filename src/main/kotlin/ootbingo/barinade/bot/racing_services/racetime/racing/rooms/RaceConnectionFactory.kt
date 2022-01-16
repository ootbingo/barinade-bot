package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

interface RaceConnectionFactory {

  fun openConnection(raceEndpoint: String)
}
