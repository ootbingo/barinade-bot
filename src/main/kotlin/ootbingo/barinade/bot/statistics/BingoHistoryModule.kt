package ootbingo.barinade.bot.statistics

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.model.ResultType

@LilyModule
class BingoHistoryModule(queryServiceFactory: QueryServiceFactory) {

  private val queryService = queryServiceFactory.newQueryService()

  @LilyCommand("results")
  fun results(chatCommand: Command): Answer<AnswerInfo>? =
      queryService.query {

        command = chatCommand

        defaultAmount = 10

        raceFilter = { s -> s.filter { it.resultType == ResultType.FINISH && it.time != null } }

        aggregator = { s ->
          s.joinToString(", ") { it.time!!.standardFormat() }
              .let { "The last Bingos of %user%: $it" }
        }
      }

  @LilyCommand("best")
  fun best(chatCommand: Command): Answer<AnswerInfo>? =
      queryService.query {

        command = chatCommand

        defaultAmount = 5

        raceFilter = { s ->
          s.sortedBy { it.time }
              .filter { it.resultType == ResultType.FINISH && it.time != null }
        }

        aggregator = { s ->
          s.joinToString(", ") { it.time!!.standardFormat() }
              .let { "%user%'s best bingos: $it" }
        }
      }

  @LilyCommand("racer")
  fun racer(chatCommand: Command): Answer<AnswerInfo>? =
      queryService.query {

        command = chatCommand

        allowDifferentAmounts = false

        aggregator = {
          val finished = it.count { r -> r.resultType == ResultType.FINISH }
          val forfeited = it.count { r -> r.resultType == ResultType.FORFEIT }

          "%user% has finished $finished bingos and forfeited $forfeited"
        }
      }
}
