package ootbingo.barinade.bot.racetime.sync

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.connection.RaceResultRepository
import ootbingo.barinade.bot.data.model.Platform
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.data.model.ResultType
import ootbingo.barinade.bot.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racetime.api.model.RacetimeRace

class RacetimeImporter(private val playerRepository: PlayerRepository,
                       private val raceRepository: RaceRepository,
                       private val raceResultRepository: RaceResultRepository) {

  fun import(race: RacetimeRace) {

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

    val player = playerRepository.save(Player(racetimeId = this.user.id, racetimeName = this.user.name))

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
}
