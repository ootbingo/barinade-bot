package ootbingo.barinade.bot.testutils

import ootbingo.barinade.bot.data.model.Player
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

interface DbUtils : Repository<Player, Long> {

  @Modifying
  @Query(value = "DROP ALL OBJECTS", nativeQuery = true)
  fun clearDatabase()
}
