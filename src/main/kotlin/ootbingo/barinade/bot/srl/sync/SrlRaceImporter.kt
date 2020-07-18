package ootbingo.barinade.bot.srl.sync

import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.model.Platform
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

    logger.info("Import races of player ${player.nameSrl}")

    val srlRaces = srlHttpClient.getRacesByPlayerName(player.nameSrl)
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
            ?.findLast { srlResult -> srlResult.player.toLowerCase() == player.nameSrl.toLowerCase() }
            ?: run {
              logger.error("No result for player ${player.nameSrl} found in race with ID ${storedRace.raceId}")
              return@forEach
            }

        storedRace.raceResults
            .add(RaceResult(RaceResult.ResultId(storedRace, player), srlResult.place, srlResult.time))
        raceRepository.save(storedRace)
      }
    }
  }

  private fun getRaceWithId(it: Race): Race {
    return raceRepository.findByRaceId(it.raceId)
        ?: Race(raceId = it.raceId, datetime = it.datetime, goal = it.goal).let { raceRepository.save(it) }
  }
}
