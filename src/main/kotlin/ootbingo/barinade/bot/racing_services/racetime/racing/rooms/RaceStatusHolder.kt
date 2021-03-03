package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace

class RaceStatusHolder {

  lateinit var race: RacetimeRace

  val raceStatus: RacetimeRace.RacetimeRaceStatus?
    get() = if (this::race.isInitialized) race.status else null

  val slug: String
    get() = if (this::race.isInitialized) race.name.split("/")[1] else ""
}
