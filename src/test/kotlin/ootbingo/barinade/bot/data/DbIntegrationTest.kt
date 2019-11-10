package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.connection.RaceResultRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.testutils.DbUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@DataJpaTest
@ExtendWith(SpringExtension::class)
internal class DbIntegrationTest(@Autowired val playerRepository: PlayerRepository,
                                 @Autowired val raceRepository: RaceRepository,
                                 @Autowired val raceResultRepository: RaceResultRepository,
                                 @Autowired val dbUtils: DbUtils) {

  @BeforeEach
  internal fun setup() {
    dbUtils.clearDatabase()
  }
}