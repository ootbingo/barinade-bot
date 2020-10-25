package ootbingo.barinade.bot.racing_services.srl.sync

import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPastRace
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["ootbingo.jobs.srl-sync.enabled"], havingValue = "true")
class SrlSyncJob(private val srlHttpClient: SrlHttpClient,
                 private val playerHelper: PlayerHelper,
                 private val raceRepository: RaceRepository,
                 private val raceResultRepository: RaceResultRepository) {

  private val logger = LoggerFactory.getLogger(SrlSyncJob::class.java)

  @Scheduled(cron = "\${ootbingo.jobs.srl-sync.cron}")
  fun execute() {
    val start = Instant.now()
    logger.info("Syncing SRL data with DB...")

    logger.info("Find all OoT races on SRL...")
    val srlRaces = srlHttpClient.getAllRacesOfGame("oot")
    logger.info("Race data downloaded.")

    syncUsers(srlRaces.allPlayerNames())
    syncRaces(srlRaces)
    syncResults(srlRaces)

    val end = Instant.now()
    logger.info("SRL Sync finished. Time: ${Duration.between(start, end).standardFormat()}")
  }

  private fun syncUsers(srlPlayers: Collection<String>) {

    logger.info("Loading players from the database...")
    val dbPlayers = playerHelper.getAllSrlPlayers()
    val dbUsernames = dbPlayers.mapNotNull { it.srlName }
    logger.info("Players loaded.")
    logger.info("Found {} players on SRL and {} SRL players in the database.", srlPlayers.size, dbUsernames.size)

    val newSrlPlayers = srlPlayers
        .filter { !dbUsernames.contains(it) }
        .mapNotNull {
          logger.info("Player {} unknown. Downloading data...", it)
          srlHttpClient.getPlayerByName(it)
        }
        .map {
          val player = dbPlayers.lastOrNull { p -> p.srlId == it.id }
          if (player != null) {
            logger.info("Player {} changed name to {}.", player.srlName, it.name)
            player.srlName = it.name
            player
          } else {
            playerHelper.getPlayerFromSrlId(it.id, it.name)
          }
        }

    logger.info("Save new and changed players to the database...")
    playerHelper.savePlayers(newSrlPlayers)
    logger.info("Players saved.")
  }

  private fun syncRaces(srlRaces: Collection<SrlPastRace>) {

    logger.info("Loading races from the database...")
    val dbRaces = raceRepository.findAll()
    val dbRaceIds = dbRaces.map { it.raceId }
    logger.info("Races loaded.")
    logger.info("Found {} races on SRL and {} races in the database.", srlRaces.size, dbRaceIds.size)

    val newSrlRaces = srlRaces
        .filter { !dbRaceIds.contains(it.id) }
        .map { Race(it.id, it.goal, it.date) }

    val changedRaces = srlRaces
        .asSequence()
        .map { it to dbRaces.lastOrNull { r -> r.raceId == it.id } }
        .filter { it.second != null }
        .map { it.first to it.second!! }
        .filter {
          it.first.date != it.second.datetime || it.first.goal != it.second.goal
        }
        .map {
          it.second.datetime = it.first.date
          it.second.goal = it.first.goal
          it.second
        }
        .toList()

    logger.info("Saving {} new and {} changed races to the database...", newSrlRaces.size, changedRaces.size)

    if (newSrlRaces.isNotEmpty()) {
      raceRepository.saveAll(newSrlRaces)
    }

    if (changedRaces.isNotEmpty()) {
      raceRepository.saveAll(changedRaces)
    }

    logger.info("Races saved.")
  }

  private fun syncResults(allRaces: Collection<SrlPastRace>) {

    logger.info("Loading incomplete races from the database...")
    val incompleteDbRaces = raceRepository.findAll()
        .filter { it.raceResults.size.toLong() != allRaces.lastOrNull { r -> r.id == it.raceId }?.numentrants }
        .filterNotNull()

    logger.info("{} races loaded.", incompleteDbRaces.size)
    logger.info("Loading all players from the database...")
    val dbPlayers = playerHelper.getAllSrlPlayers()
    logger.info("Players loaded.")

    fun getPlayerWithUsername(username: String) =
        dbPlayers.last { it.srlName == username }

    val allResultsOfIncompleteRaces = incompleteDbRaces
        .map { race -> race to allRaces.lastOrNull { it.id == race.raceId } }
        .filter { it.second != null }
        .map { it.first to it.second!! }
        .flatMap {
          it.second.results.map { result ->
            RaceResult(RaceResult.ResultId(it.first, getPlayerWithUsername(result.player)),
                       result.place,
                       result.time,
                       if (result.time.isNegative) ResultType.FORFEIT else ResultType.FINISH)
          }
        }

    logger.info("Loading all results from the database...")
    val dbResults = raceResultRepository.findAll()
    logger.info("{} results loaded.", dbResults.count())
    logger.info("Check for duplicates...")

    val newSrlResults = allResultsOfIncompleteRaces
        .filter {
          dbResults
              .filter { dbResult -> dbResult.resultId.race == it.resultId.race }
              .none { dbResult -> dbResult.resultId.player == it.resultId.player }
        }

    logger.info("Saving {} new race results to the database...", newSrlResults.size)
    raceResultRepository.saveAll(newSrlResults)
    logger.info("Results saved.")
  }
}
