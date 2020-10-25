package ootbingo.barinade.bot.racing_services.data.connection

import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Race
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
interface RaceRepository : CrudRepository<Race, String> {

  @Fetch(FetchMode.JOIN)
  fun findByRaceId(id: String): Race?

  fun findAllByPlatform(platform: Platform): Collection<Race>
}
