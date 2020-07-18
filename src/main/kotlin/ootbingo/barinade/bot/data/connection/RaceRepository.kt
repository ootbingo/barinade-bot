package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Race
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
interface RaceRepository : CrudRepository<Race, String> {

  @Fetch(FetchMode.JOIN)
  fun findByRaceId(id: String): Race?
}
