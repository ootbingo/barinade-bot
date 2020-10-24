package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace

class RacetimeImporter(private val playerHelper: PlayerHelper,
                       private val raceRepository: RaceRepository,
                       private val raceResultRepository: RaceResultRepository) {

  fun import(races: Collection<RacetimeRace>) =
      races.forEach(this::import)

  fun import(race: RacetimeRace) {

    if (race.goal.custom || race.goal.name != "Bingo") {
      return
    }

    val dbRace = saveNewRace(race)
    race.entrants.forEach {
      saveNewResult(it, dbRace)
    }
  }

  private fun saveNewRace(race: RacetimeRace) =
      raceRepository.save(Race(race.name, race.info, race.endedAt!!, Platform.RACETIME, mutableListOf()))

  private fun saveNewResult(entrant: RacetimeEntrant, race: Race): RaceResult {

    val player = entrant.let { playerHelper.getPlayerFromRacetimeId(it.user.id, it.user.name) }

    return raceResultRepository.save(
        RaceResult(
            RaceResult.ResultId(race, player),
            entrant.place?.toLong() ?: -1,
            entrant.finishTime,
            when (entrant.status) {
              RacetimeEntrant.RacetimeEntrantStatus.DONE -> ResultType.FINISH
              RacetimeEntrant.RacetimeEntrantStatus.DNF -> ResultType.FORFEIT
              else -> ResultType.DQ
            }
        )
    )
  }
}
