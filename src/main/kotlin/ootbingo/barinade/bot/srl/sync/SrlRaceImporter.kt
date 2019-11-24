package ootbingo.barinade.bot.srl.sync

import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SrlRaceImporter(private val srlHttpClient: SrlHttpClient,
                      private val raceRepository: RaceRepository) {

  private val logger = LoggerFactory.getLogger(SrlRaceImporter::class.java)

  private val whitelistedGames by lazy {
    listOf("oot", "ootbingo")
        .mapNotNull { srlHttpClient.getGameByAbbreviation(it) }
        .toList()
  }

  fun importRacesForUser(player: Player) {

    logger.info("Import races of player ${player.srlName}")

    val srlRaces = srlHttpClient.getRacesByPlayerName(player.srlName)
        .filter { it.game in whitelistedGames }

    val emptyRaces = srlRaces
        .map { Race(it.id, it.goal, it.date, it.numentrants, mutableListOf()) }
        .toMutableList()

    emptyRaces.forEach {

      val maybeStoredRace = raceRepository.findBySrlId(it.srlId)
          ?: Race(srlId = it.srlId, recordDate = it.recordDate, goal = it.goal)
      val storedRace = raceRepository.save(maybeStoredRace)

      if (storedRace.raceResults.none { result -> result.player == player }) {

        val srlResult = srlRaces
            .findLast { srlRace -> srlRace.id == it.srlId }
            ?.results
            ?.findLast { srlResult -> srlResult.player.toLowerCase() == player.srlName.toLowerCase() }
            ?: run {
              logger.error("No result for player ${player.srlName} found in race with ID ${storedRace.srlId}")
              return@forEach
            }

        storedRace.raceResults
            .add(RaceResult(null, storedRace, player, srlResult.place, srlResult.time, srlResult.message))
        raceRepository.save(storedRace)
      }
    }
  }
}
