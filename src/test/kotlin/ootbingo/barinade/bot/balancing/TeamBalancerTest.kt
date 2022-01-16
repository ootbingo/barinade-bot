package ootbingo.barinade.bot.balancing

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import java.time.Duration

internal class TeamBalancerTest {

  private val balancer = TeamBalancer()

  @Test
  internal fun balancesTwoTeamsCorrectly() {

    val actualTeams = balancer.findBestTeamBalance(listOf(partition(team(10), team(20)),
        partition(team(100), team(101))))

    thenBalancedTeamsHaveExpectedTimes(actualTeams, 100, 101)
  }

  private fun team(predictedTime: Long): Team {

    val teamMock = mock<Team>()
    `when`(teamMock.predictedTime).thenReturn(Duration.ofSeconds(predictedTime))

    return teamMock
  }

  private fun partition(vararg teams: Team): List<Team> {
    return teams.asList()
  }

  private fun thenBalancedTeamsHaveExpectedTimes(actualTeams: List<Team>, vararg expectedTimes: Long) {

    assertThat(actualTeams.map { it.predictedTime.toSeconds() })
        .containsExactlyInAnyOrder(*expectedTimes.toTypedArray())
  }
}
