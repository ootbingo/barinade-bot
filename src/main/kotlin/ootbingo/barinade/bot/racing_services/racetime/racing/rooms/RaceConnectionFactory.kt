package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

fun interface RaceConnectionFactory {

  fun openConnection(raceEndpoint: String)
}
