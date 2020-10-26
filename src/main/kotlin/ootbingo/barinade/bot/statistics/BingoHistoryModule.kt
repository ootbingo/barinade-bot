package ootbingo.barinade.bot.statistics

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.getUsername
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.ResultType

@LilyModule
class BingoHistoryModule(private val playerHelper: PlayerHelper) {

  @LilyCommand("results")
  fun results(command: Command): Answer<AnswerInfo>? {

    val queryMetadata = command.asQueryMetadata()
    val races = playerHelper.findResultsForPlayer(playerHelper.getPlayerByName(queryMetadata.username)!!)
        .asSequence()
        .filter { it.resultType == ResultType.FINISH && Race(it.raceId, it.goal, it.datetime).isBingo() }
        .mapNotNull { it.time?.standardFormat() }
        .take(queryMetadata.amount)
        .joinToString(", ")

    return Answer.ofText("The last Bingos of ${queryMetadata.username}: $races")
  }

  @LilyCommand("best")
  fun best(command: Command): Answer<AnswerInfo>? {

    val queryMetadata = command.asQueryMetadata(defaultAmount = 5)
    val races = playerHelper.findResultsForPlayer(playerHelper.getPlayerByName(queryMetadata.username)!!)
        .sortedBy { it.time }
        .asSequence()
        .filter { it.resultType == ResultType.FINISH && Race(it.raceId, it.goal, it.datetime).isBingo() }
        .mapNotNull { it.time?.standardFormat() }
        .take(queryMetadata.amount)
        .joinToString(", ")

    return Answer.ofText("${queryMetadata.username}'s best bingos: $races")
  }

  private fun Command.asQueryMetadata(defaultAmount: Int = 10): QueryMetadata {

    fun parseSingleArgument(argument: String): QueryMetadata {
      return try {
        messageInfo.getUsername()?.let { QueryMetadata(it, argument.toInt()) } ?: throw IllegalArgumentException()
      } catch (e: NumberFormatException) {
        QueryMetadata(argument, defaultAmount)
      }
    }

    fun parseDoubleArgument(first: String, second: String): QueryMetadata {
      return try {
        QueryMetadata(first, second.toInt())
      } catch (e: NumberFormatException) {
        QueryMetadata(second, first.toInt())
      }
    }

    return when (argumentCount) {
      0 -> messageInfo.getUsername()?.let { QueryMetadata(it, defaultAmount) } ?: throw IllegalArgumentException()
      1 -> parseSingleArgument(getArgument(0))
      else -> parseDoubleArgument(getArgument(0), getArgument(1))
    }
  }

  private class QueryMetadata(val username: String, val amount: Int)
}
