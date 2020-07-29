package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import org.springframework.stereotype.Component

@Component
class PlayerDao(private val playerRepository: PlayerRepository) {

  fun getPlayerByName(name: String): Player? =
      playerRepository.findByRacetimeNameIgnoreCase(name)
          ?: playerRepository.findBySrlNameIgnoreCase(name)

  fun findResultsForPlayer(player: Player): List<ResultInfo> =
      playerRepository.findResultsForPlayer(player)
}
