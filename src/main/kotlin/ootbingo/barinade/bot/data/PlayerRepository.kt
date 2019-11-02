package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.model.Player
import ootbingo.barinade.bot.model.Race
import ootbingo.barinade.bot.model.RaceResult
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class PlayerRepository(private val srlHttpClient: SrlHttpClient) {

  private val whitelistedGames by lazy {
    listOf("oot", "ootbingo")
        .mapNotNull { srlHttpClient.getGameByAbbreviation(it) }
        .toList()
  }

  fun getPlayerByName(name: String): Player? {

    val srlPlayer = srlHttpClient.getPlayerByName(name) ?: return null
    val races = srlHttpClient.getRacesByPlayerName(srlPlayer.name)
        .filter { it.game in whitelistedGames }
        .map {
          Race(it.id, it.goal, it.date, it.numentrants,
               it.results.map { result ->
                 RaceResult(Race("", "", ZonedDateTime.now(), 0, emptyList()),
                            Player(0, result.player, emptyList()),
                            result.place, result.time, result.message)
               })
        }

    races.forEach {
      it.raceResults.forEach { result -> result.race = it }
    }

    return Player(srlPlayer, races)
  }
}
