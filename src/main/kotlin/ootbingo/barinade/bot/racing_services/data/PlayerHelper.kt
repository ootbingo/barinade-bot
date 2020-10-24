package ootbingo.barinade.bot.racing_services.data

import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.springframework.stereotype.Component

@Component
class PlayerHelper(private val playerRepository: PlayerRepository) {

  fun getPlayerByName(name: String): Player? =
      playerRepository.findByRacetimeNameIgnoreCase(name)
          ?: playerRepository.findBySrlNameIgnoreCase(name)

  fun findResultsForPlayer(player: Player): List<ResultInfo> =
      playerRepository.findResultsForPlayer(player)

  fun getPlayerFromRacetimeId(racetimeId: String, racetimeName: String) =
      playerRepository.findByRacetimeId(racetimeId)
          ?: playerRepository.save(
              Player(racetimeId = racetimeId, racetimeName = racetimeName))
}
