package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Race
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface RaceRepository : Repository<Race, String> {

  fun save(race: Race): Race
  fun findBySrlId(srlId: String): Race?
}