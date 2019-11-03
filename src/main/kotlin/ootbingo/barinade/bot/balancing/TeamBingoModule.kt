package ootbingo.barinade.bot.balancing

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.allTeamPartitions
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.statistics.BingoStatModule

@LilyModule
class TeamBingoModule(private val bingoStatModule: BingoStatModule, private val teamBalancer: TeamBalancer,
                      private val partitioner: (List<TeamMember>, Int) -> List<List<Team>> = List<TeamMember>::allTeamPartitions) {

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
