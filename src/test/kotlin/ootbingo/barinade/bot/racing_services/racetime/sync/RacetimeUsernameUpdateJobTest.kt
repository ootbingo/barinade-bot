package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeLeaderboard
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeLeaderboardEntry
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@SpringBootTest
class RacetimeUsernameUpdateJobTest(
  @Autowired private val playerRepository: PlayerRepository,
) {

  //<editor-fold desc="Setup">

  private val racetimeHttpClientMock: RacetimeHttpClient = mock()

  private val job = RacetimeUsernameUpdateJob(playerRepository, racetimeHttpClientMock)

  //</editor-fold>

  @Test
  internal fun updatesUsernames() {

    val (oldUsername1, oldUsername2, newUsername1, newUsername2, unchangedUsername) = (1..5).map {
      UUID.randomUUID().toString()
    }

    val (changedId1, changedId2, unchangedId) = (1..3).map { UUID.randomUUID().toString() }

    whenever(racetimeHttpClientMock.getLeaderboards()).thenReturn(
      listOf(
        RacetimeLeaderboard(
          "Bingo", listOf(
            RacetimeLeaderboardEntry(RacetimeUser(changedId1, newUsername1)),
            RacetimeLeaderboardEntry(RacetimeUser(unchangedId, unchangedUsername)),
            RacetimeLeaderboardEntry(RacetimeUser(UUID.randomUUID().toString(), UUID.randomUUID().toString())),
          )
        ),
        RacetimeLeaderboard(
          "Bingo", listOf(
            RacetimeLeaderboardEntry(RacetimeUser(changedId1, newUsername1)),
            RacetimeLeaderboardEntry(RacetimeUser(changedId2, newUsername2)),
            RacetimeLeaderboardEntry(RacetimeUser(unchangedId, unchangedUsername)),
            RacetimeLeaderboardEntry(RacetimeUser(UUID.randomUUID().toString(), UUID.randomUUID().toString())),
          )
        ),
      )
    )

    val player1 = playerRepository.save(Player(null, null, changedId1, null, oldUsername1))
    val player2 = playerRepository.save(Player(null, null, changedId2, null, oldUsername2))
    val unchangedPlayer = playerRepository.save(Player(null, null, unchangedId, null, unchangedUsername))

    job.execute()

    val allPlayers = playerRepository.findAll()

    assertThat(allPlayers).hasSize(3)

    listOf(
      player1.copy(racetimeName = newUsername1),
      player2.copy(racetimeName = newUsername2),
      unchangedPlayer,
    ).forEach { player ->
      assertThat(allPlayers).anyMatch { it.id == player.id && it.racetimeId == player.racetimeId && it.racetimeName == player.racetimeName }
    }
  }

  @Test
  internal fun noChangeIfRequestFails() {

    whenever(racetimeHttpClientMock.getLeaderboards()).thenThrow(RuntimeException::class.java)

    val player =
      playerRepository.save(Player(null, null, UUID.randomUUID().toString(), null, UUID.randomUUID().toString()))

    val allPlayers = playerRepository.findAll()

    assertThat(allPlayers).hasSize(1)
    assertThat(allPlayers).anyMatch { it.id == player.id && it.racetimeId == player.racetimeId && it.racetimeName == player.racetimeName }
  }
}
