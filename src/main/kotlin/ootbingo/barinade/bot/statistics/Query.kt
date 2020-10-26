package ootbingo.barinade.bot.statistics

import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.getUsername
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo

class QueryDefinition(
    var command: Command? = null,
    var defaultAmount: Int? = null,
    var allowDifferentAmounts: Boolean = true,
    var raceFilter: (Sequence<ResultInfo>) -> Sequence<ResultInfo> = { it },
    var aggregator: (Sequence<ResultInfo>) -> String = { "" }
) {

  fun validate(): Unit? {
    if (command == null) {
      return null
    }

    return Unit
  }
}

fun query(playerHelper: PlayerHelper, block: QueryDefinition.() -> Unit): Answer<AnswerInfo>? {

  val definition = QueryDefinition().apply(block)
  definition.validate() ?: return null

  val (queryUser, queryAmount) = definition.command!!.asQueryMetadata()
  val amount = if (definition.allowDifferentAmounts) {
    queryAmount ?: definition.defaultAmount
  } else {
    definition.defaultAmount
  }

  fun Sequence<ResultInfo>.ifAmount(action: Sequence<ResultInfo>.() -> Sequence<ResultInfo>): Sequence<ResultInfo> {
    return if (amount != null) {
      this.action()
    } else {
      this
    }
  }

  return playerHelper.findResultsForPlayer(playerHelper.getPlayerByName(queryUser)!!)
      .asSequence()
      .run(definition.raceFilter)
      .ifAmount { take(amount!!) }
      .run(definition.aggregator)
      .let { Answer.ofText(it.replace("%user%", queryUser).also { println(it) }) }
}

private data class QueryMetadata(val username: String, val amount: Int?)

private fun Command.asQueryMetadata(defaultAmount: Int? = null): QueryMetadata {

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
