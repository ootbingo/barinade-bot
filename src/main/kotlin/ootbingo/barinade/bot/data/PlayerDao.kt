package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import ootbingo.barinade.bot.srl.sync.SrlPlayerImporter
import org.springframework.stereotype.Component

@Component
class PlayerDao(private val playerRepository: PlayerRepository,
                private val playerImporter: SrlPlayerImporter) {

  fun getPlayerByName(name: String): Player? =
      playerRepository.findBySrlNameIgnoreCase(name) ?: playerImporter.importPlayer(name)

  fun findResultsForPlayer(player: Player): List<ResultInfo> =
      playerRepository.findResultsForPlayer(player)
}
