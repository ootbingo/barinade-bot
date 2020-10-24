package ootbingo.barinade.bot.racing_services.racetime.sync

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

class RacetimeImporter(private val playerRepository: PlayerRepository,
                       private val raceRepository: RaceRepository,
                       private val raceResultRepository: RaceResultRepository) {

  fun import(races: Collection<RacetimeRace>) =
      races.forEach(this::import)

  fun import(race: RacetimeRace) {

    if (race.goal.custom || race.goal.name != "Bingo") {
      return
    }

    val dbRace = raceRepository.save(race.toDbRace())
    race.entrants.forEach {
      raceResultRepository.save(it.toDbResult(dbRace))
    }
  }

  private fun RacetimeRace.toDbRace(): Race {

    return Race(
        name,
        info,
        endedAt!!,
        Platform.RACETIME,
        mutableListOf()
    )
  }

  private fun RacetimeEntrant.toDbResult(race: Race): RaceResult {

    val player = getPlayerFromRacetimeEntrant(this)

    return RaceResult(
        RaceResult.ResultId(race, player),
        place?.toLong() ?: -1,
        finishTime,
        when (status) {
          RacetimeEntrant.RacetimeEntrantStatus.DONE -> ResultType.FINISH
          RacetimeEntrant.RacetimeEntrantStatus.DNF -> ResultType.FORFEIT
          else -> ResultType.DQ
        }
    )
  }

  private fun getPlayerFromRacetimeEntrant(racetimeEntrant: RacetimeEntrant) =
      playerRepository.findByRacetimeId(racetimeEntrant.user.id)
          ?: playerRepository.save(
              Player(racetimeId = racetimeEntrant.user.id, racetimeName = racetimeEntrant.user.name))
}
