package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.RaceResult
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
interface RaceResultRepository : CrudRepository<RaceResult, Long> {

//  fun save(raceResult: RaceResult): RaceResult
//  fun save(raceResults: Collection<RaceResult>)
//  fun findAll(): List<RaceResult>
}
