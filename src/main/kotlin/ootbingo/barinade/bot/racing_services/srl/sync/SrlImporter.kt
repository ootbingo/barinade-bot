package ootbingo.barinade.bot.racing_services.srl.sync

import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.srl.api.client.SrlHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SrlImporter(private val srlHttpClient: SrlHttpClient,
                  private val raceRepository: RaceRepository) {

  private val logger = LoggerFactory.getLogger(SrlImporter::class.java)

  private val whitelistedGames by lazy {
    listOf("oot", "ootbingo")
        .mapNotNull { srlHttpClient.getGameByAbbreviation(it) }
        .toList()
  }

  fun importRacesForUser(player: Player) {

    if (player.srlId == null || player.srlName == null) {
      logger.error("Player is not on SRL")
      return
    }

    logger.info("Import races of player ${player.srlName}")

    val srlRaces = srlHttpClient.getRacesByPlayerName(player.srlName!!)
        .filter { it.game in whitelistedGames }

    val emptyRaces = srlRaces
        .map { Race(it.id, it.goal, it.date, Platform.SRL, mutableListOf()) }
        .toMutableList()

    emptyRaces.forEach {

      val storedRace = getRaceWithId(it)

      if (storedRace.raceResults.none { result -> result.resultId.player == player }) {

        val srlResult = srlRaces
            .findLast { srlRace -> srlRace.id == it.raceId }
            ?.results
            ?.findLast { srlResult -> srlResult.player.toLowerCase() == player.srlName!!.toLowerCase() }
            ?: run {
              logger.error("No result for player ${player.srlName} found in race with ID ${storedRace.raceId}")
              return@forEach
            }

        val resultType = when (srlResult.place) {
          9998L -> ResultType.FORFEIT
          9999L -> ResultType.DQ
          else -> ResultType.FINISH
        }

        storedRace.raceResults
            .add(RaceResult(RaceResult.ResultId(storedRace, player), srlResult.place, srlResult.time, resultType))
        raceRepository.save(storedRace)
      }
    }
  }

  private fun getRaceWithId(it: Race): Race {
    return raceRepository.findByRaceId(it.raceId)
        ?: Race(raceId = it.raceId, datetime = it.datetime, goal = it.goal).let { raceRepository.save(it) }
  }
}
