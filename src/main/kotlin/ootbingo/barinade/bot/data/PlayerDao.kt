package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.springframework.stereotype.Component

@Component
class PlayerDao(private val srlHttpClient: SrlHttpClient,
                private val playerRepository: PlayerRepository,
                private val raceRepository: RaceRepository) {

  private val whitelistedGames by lazy {
    listOf("oot", "ootbingo")
        .mapNotNull { srlHttpClient.getGameByAbbreviation(it) }
        .toList()
  }

  fun getPlayerByName(name: String): Player? {

    with(playerRepository.findBySrlNameIgnoreCase(name)) {
      if (this != null) {
        return this
      }
    }

    val srlPlayer = srlHttpClient.getPlayerByName(name) ?: return null

    val emptyPlayer = Player(srlPlayer, mutableListOf())
    val player = playerRepository.save(emptyPlayer)

    val srlRaces = srlHttpClient.getRacesByPlayerName(srlPlayer.name)
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
            ?: return player

        storedRace.raceResults
            .add(RaceResult(null, storedRace, player, srlResult.place, srlResult.time, srlResult.message))
        raceRepository.save(storedRace)
      }
    }

    return playerRepository.findBySrlNameIgnoreCase(player.srlName)
  }

  fun findResultsForPlayer(username: String): List<ResultInfo>? =
      playerRepository.findResultsForPlayer(username)
}
