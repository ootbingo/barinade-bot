package ootbingo.barinade.bot.balancing

import ootbingo.barinade.bot.extensions.allTeamPartitions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BalancingConfiguration {

  @Bean
  fun partitioner(): (List<TeamMember>, Int) -> List<List<Team>> =
      List<TeamMember>::allTeamPartitions
}
