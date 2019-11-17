package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class PlayerDao(private val srlHttpClient: SrlHttpClient,
                private val playerRepository: PlayerRepository) {

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
    val races = srlHttpClient.getRacesByPlayerName(srlPlayer.name)
        .filter { it.game in whitelistedGames }
        .map {
          Race(it.id, it.goal, it.date, it.numentrants,
               it.results.map { result ->
                 RaceResult(0L, Race("", "", ZonedDateTime.now(), 0, mutableListOf()),
                            Player(0, result.player, mutableListOf()),
                            result.place, result.time, result.message)
               }.toMutableList())
        }

    races.forEach {
      it.raceResults.forEach { result -> result.race = it }
    }

    val player =  Player(srlPlayer, races)
    playerRepository.save(player)

    return player
  }
}
