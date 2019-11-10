package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.RaceResult
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface RaceResultRepository : Repository<RaceResult, Long> {

  fun save(raceResult: RaceResult)
}