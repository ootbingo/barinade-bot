package ootbingo.barinade.bot.srl.sync

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SrlPlayerImporter(private val srlHttpClient: SrlHttpClient,
                        private val raceImporter: SrlRaceImporter,
                        private val playerRepository: PlayerRepository) {

  private val logger = LoggerFactory.getLogger(SrlPlayerImporter::class.java)

  fun importPlayer(username: String): Player? {

    logger.info("Import of player $username requested")

    val srlPlayer = srlHttpClient.getPlayerByName(username) ?: run {
      logger.info("Player $username not found")
      return null
    }

    val emptyPlayer = Player(srlPlayer, mutableListOf())
    val player = playerRepository.save(emptyPlayer)

    raceImporter.importRacesForUser(player)

    return playerRepository.findByNameSrlIgnoreCase(player.nameSrl)
  }
}
