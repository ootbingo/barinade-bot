package ootbingo.barinade.bot.modules

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.standardFormat
import java.time.Duration
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

@LilyModule
class TeamBingoModule(private val bingoStatModule: BingoStatModule) {

  private val setupTime = Duration.ofMinutes(30).toSeconds()
  private val blackoutRatio = 3.3
  private val overlap = Duration.ofMinutes(5).toSeconds()
  private val forfeitThresholdFactor =
      Duration.ofHours(1).plusMinutes(40).toSeconds().toDouble() / Duration.ofHours(1).plusMinutes(20).toSeconds().toDouble()

  @LilyCommand("teamtime")
  fun teamTime(command: Command): Answer<AnswerInfo>? {

    when (command.argumentCount) {
      0 -> return Answer.ofText("No players supplied")

      in 1..4 -> {
      }

      else -> return Answer.ofText("Unsupported amount of players supplied")
    }

    val members = (0 until command.argumentCount)
        .map { command.getArgument(it) }
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

    with(members.filter { it.workRate == null }) {
      if (this.isNotEmpty()) {
        return Answer.ofText("Error retrieving data of user(s): " + this.joinToString(", ") { it.name })
      }
    }

    val combinedWorkRate = members
        .mapNotNull { it.workRate }
        .sum()

    val blackoutAverage = (1 / combinedWorkRate)
        .let { it * blackoutRatio }
        .let { it + setupTime + overlap }
        .roundToLong()
        .let { Duration.ofSeconds(it) }
        .standardFormat()

    return Answer.ofText(
        members
            .filter { it.workRate != null }
            .joinToString(", ") { it.name }
            .let { "$it can finish a blackout in: $blackoutAverage" })
  }

  private inner class TeamMember(val name: String, median: Long?, forfeitRatio: Double?) {
    val workRate: Double? = median
        ?.let { it * (1 + 2 * min(forfeitRatio!!, 0.5) * (forfeitThresholdFactor - 1)) }
        ?.let { (1 / (it - setupTime)) }
  }
}
