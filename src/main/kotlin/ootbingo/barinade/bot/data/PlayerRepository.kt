package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.model.Player
import ootbingo.barinade.bot.model.Race
import ootbingo.barinade.bot.model.RaceResult
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class PlayerRepository(private val srlHttpClient: SrlHttpClient, private val clock: Clock = Clock.systemUTC()) {

  private val playerCache = mutableMapOf<String, Player>()
  private var lastSaved = clock.instant()

  private val whitelistedGames by lazy {
    listOf("oot", "ootbingo")
        .mapNotNull { srlHttpClient.getGameByAbbreviation(it) }
        .toList()
  }

  fun getPlayerByName(name: String): Player? {

    if (lastSaved.isBefore(clock.instant().truncatedTo(ChronoUnit.DAYS))) {
      playerCache.clear()
      lastSaved = clock.instant()
    }

    with(playerCache[name.toLowerCase()]) {
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
                 RaceResult(Race("", "", ZonedDateTime.now(), 0, emptyList()),
                            Player(0, result.player, emptyList()),
                            result.place, result.time, result.message)
               })
        }

    races.forEach {
      it.raceResults.forEach { result -> result.race = it }
    }

    val player =  Player(srlPlayer, races)
    playerCache[player.name.toLowerCase()] = player

    return player
  }
}
