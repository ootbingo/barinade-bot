package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.RaceResult
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface RaceResultRepository : Repository<RaceResult, Long> {

  fun save(raceResult: RaceResult): RaceResult
  fun save(raceResults: Collection<RaceResult>)
  fun findAll(): List<RaceResult>
  fun findAllByPlayer(player: Player): Set<RaceResult>
}
