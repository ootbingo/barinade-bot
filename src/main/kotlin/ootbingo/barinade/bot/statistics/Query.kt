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
    var aggregator: (List<ResultInfo>) -> String = { "" }
) {

  fun validate(): Unit? {
    if (command == null) {
      return null
    }

    return Unit
  }
}

fun PlayerHelper.query(block: QueryDefinition.() -> Unit): Answer<AnswerInfo>? {

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

  fun List<ResultInfo>.checkForBingos(): List<ResultInfo>? {
    return if (this.none { it.isBingo() }) {
      null
    } else {
      this
    }
  }

  var count = -1

  return (this.getPlayerByName(queryUser) ?: return Answer.ofText("User $queryUser not found"))
      .let { this.findResultsForPlayer(it) }
      .checkForBingos()
      ?.asSequence()
      ?.run(definition.raceFilter)
      ?.ifAmount { take(amount!!) }
      ?.toList()
      ?.also { count = it.count() }
      ?.run(definition.aggregator)
      ?.let { Answer.ofText(
          it.replace("%user%", queryUser).replace("%count%", count.toString())
      ) }
      ?: return Answer.ofText("$queryUser has not finished any bingos")
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
