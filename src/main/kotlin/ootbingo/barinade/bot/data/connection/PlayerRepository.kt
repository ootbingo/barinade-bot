package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Player
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface PlayerRepository : Repository<Player, Long> {

  fun save(player: Player): Player
  fun findBySrlNameIgnoreCase(srlName: String): Player?
}
