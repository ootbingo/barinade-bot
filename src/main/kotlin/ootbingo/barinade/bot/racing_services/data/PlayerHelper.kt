package ootbingo.barinade.bot.racing_services.data

import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.springframework.stereotype.Component

@Component
class PlayerHelper(
    private val playerRepository: PlayerRepository,
    private val usernameMapper: UsernameMapper,
) {

  fun getPlayerByName(name: String): Player? =
      playerRepository.findByRacetimeNameIgnoreCase(name)
          ?: playerRepository.findBySrlNameIgnoreCase(name)

  fun findResultsForPlayer(player: Player): List<ResultInfo> =
      playerRepository.findResultsForPlayer(player)

  fun getPlayerFromRacetimeId(racetimeId: String, racetimeName: String): Player =
      playerRepository.findByRacetimeId(racetimeId)
          ?: playerRepository.findBySrlNameIgnoreCase(usernameMapper.racetimeToSrl(racetimeName))
              ?.updateWithData(racetimeId = racetimeId, racetimeName = racetimeName)
          ?: playerRepository.save(
              Player(racetimeId = racetimeId, racetimeName = racetimeName))

  fun getPlayerFromSrlId(srlId: Long, srlName: String): Player =
      playerRepository.findBySrlId(srlId)
          ?: playerRepository.findByRacetimeNameIgnoreCase(usernameMapper.srlToRacetime(srlName))
              ?.updateWithData(srlId = srlId, srlName = srlName)
          ?: playerRepository.save(
              Player(srlId = srlId, srlName = srlName))

  fun Player.updateWithData(
      racetimeId: String? = null, racetimeName: String? = null,
      srlId: Long? = null, srlName: String? = null,
  ): Player {

    var changed = false

    if (racetimeId != null && this.racetimeId != racetimeId) {
      changed = true
      this.racetimeId = racetimeId
    }

    if (racetimeName != null && this.racetimeName != racetimeName) {
      changed = true
      this.racetimeName = racetimeName
    }

    if (srlId != null && this.srlId != srlId) {
      changed = true
      this.srlId = srlId
    }

    if (srlName != null && this.srlName != srlName) {
      changed = true
      this.srlName = srlName
    }

    return if (changed) {
      playerRepository.save(this)
    } else {
      this
    }
  }

  fun getAllRacetimePlayers(): Collection<Player> =
      playerRepository
          .findAll()
          .filter { it.racetimeId != null }
          .toSet()

  fun getAllSrlPlayers(): Collection<Player> =
      playerRepository
          .findAll()
          .filter { it.srlId != null }
          .toSet()

  fun savePlayers(players: Collection<Player>) {
    playerRepository.saveAll(players)
  }
}
