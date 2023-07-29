package ootbingo.barinade.bot.balancing

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import ootbingo.barinade.bot.statistics.BingoStatModule

@LilyModule
class TeamBingoModule(
    private val bingoStatModule: BingoStatModule, private val teamBalancer: TeamBalancer,
    private val partitioner: (List<TeamMember>, Int) -> List<List<Team>>,
) {

  @LilyCommand("teamtime")
  fun teamTime(command: Command): Answer<AnswerInfo>? {

    val team = findMembers(
        when (command.argumentCount) {
          0 -> return Answer.ofText("No players supplied")
          in 1..4 -> (1..command.argumentCount).map { command.getArgument(it - 1) }

          else -> return Answer.ofText("Unsupported amount of players supplied")
        }
    )

    with(team.members.filter { it.workRate == null }) {
      if (this.isNotEmpty()) {
        return Answer.ofText("Error retrieving data of user(s): " + this.joinToString(", ") { it.name })
      }
    }

    val blackoutAverage = team.predictedTime.standardFormat()

    return Answer.ofText(
        team.members
            .filter { it.workRate != null }
            .joinToString(", ") { it.name }
            .let { "$it can finish a blackout in: $blackoutAverage" })
  }

  @LilyCommand("balance")
  fun balance(command: Command): Answer<AnswerInfo>? {

    if (command.messageInfo is RacetimeMessageInfo) {
      return Answer.ofText("Due to character limits on racetime.gg balancing is only available on Discord")
    }

    val maxTeamSize = when (command.argumentCount) {
      in 0..3 -> return Answer.ofText("Please specify at least four players.")
      4 -> 2
      in 5..12 -> 3
      else -> return Answer.ofText("Please specify at most twelve players.")
    }

    val participants = findMembers((1..command.argumentCount).map { command.getArgument(it - 1) })

    with(participants.members.filter { it.workRate == null }) {
      if (this.isNotEmpty()) {
        return Answer.ofText("Error retrieving data of user(s): " + this.joinToString(", ") { it.name })
      }
    }

    val teams = teamBalancer.findBestTeamBalance(partitioner.invoke(participants.members, maxTeamSize))

    return Answer.ofText(teams.joinToString("\n"))
  }

  private fun findMembers(commandArguments: List<String>): Team {

    return Team(
        commandArguments
            .map { arg ->
              when {
                arg.matches(Regex("\\d+:\\d\\d:\\d\\d")) ->
                  arg.split(':')
                      .map { it.toLong() }
                      .let { it[2] + 60 * it[1] + 3600 * it[0] }
                      .let { TeamMember(arg, it, 0.0) }

                else -> bingoStatModule.median(arg)
                    ?.let { TeamMember(arg, it.toSeconds(), bingoStatModule.forfeitRatio(arg)!!) }
                    ?: TeamMember(arg, null, null)
              }
            }
    )
  }
}
