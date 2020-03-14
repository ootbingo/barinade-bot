package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Race
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
interface RaceRepository : CrudRepository<Race, String> {

//  fun save(race: Race): Race
//  fun save(races: Collection<Race>)
  fun findBySrlId(srlId: String): Race?
//  fun findAll(): Set<Race>
}
