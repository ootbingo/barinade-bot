package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.slf4j.LoggerFactory

@Open
class RacetimeImporter(private val playerHelper: PlayerHelper,
                       private val raceRepository: RaceRepository,
                       private val raceResultRepository: RaceResultRepository) {

  private val logger = LoggerFactory.getLogger(RacetimeImporter::class.java)

  private val playerCache =
      playerHelper
          .also { logger.info("Loading Racetime players from DB...") }
          .getAllRacetimePlayers()
          .map { it.racetimeId!! to it }
          .toMap()
          .toMutableMap()
          .also { logger.info("{} Racetime players found.", it.size) }

  private val raceIds =
      raceRepository
          .also { logger.info("Loading Racetime races from DB") }
          .findAllByPlatform(Platform.RACETIME)
          .map { it.raceId }
          .also { logger.info("{} Racetime races found.", it.size) }

  fun import(races: Collection<RacetimeRace>) =
      races.forEach(this::import)

  private fun import(race: RacetimeRace) {

    if (race.goal.custom || race.goal.name != "Bingo" || raceIds.contains(race.name)) {
      return
    }

    logger.info("Importing race ${race.name}...")
    val dbRace = saveNewRace(race)

    race.entrants.forEach {
      saveNewResult(it, dbRace)
    }
  }

  private fun saveNewRace(race: RacetimeRace) =
      raceRepository.save(Race(race.name, race.info, race.endedAt!!, Platform.RACETIME, mutableListOf()))

  private fun saveNewResult(entrant: RacetimeEntrant, race: Race): RaceResult {

    val player = entrantToPlayer(entrant)

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

  private fun entrantToPlayer(entrant: RacetimeEntrant): Player =
      playerCache[entrant.user.name]
          ?: entrant.let { playerHelper.getPlayerFromRacetimeId(it.user.id, it.user.name) }
              .also { playerCache[entrant.user.name] = it }
}
