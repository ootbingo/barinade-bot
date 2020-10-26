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
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo

@LilyModule
class BingoHistoryModule(private val playerHelper: PlayerHelper) {

  @LilyCommand("results")
  fun results(chatCommand: Command): Answer<AnswerInfo>? =
      query(playerHelper) {

        command = chatCommand

        defaultAmount = 10

        raceFilter = { s ->
          s.filter { it.resultType == ResultType.FINISH && Race(it.raceId, it.goal, it.datetime).isBingo() }
              .filter { it.time != null }
        }

        aggregator = { s ->
          s.joinToString(", ") { it.time!!.standardFormat() }
              .let { "The last Bingos of %user%: $it" }
        }
      }

  @LilyCommand("best")
  fun best(chatCommand: Command): Answer<AnswerInfo>? =
      query(playerHelper) {

        command = chatCommand

        defaultAmount = 5

        raceFilter = { s ->
          s.sortedBy { it.time }
              .filter { it.resultType == ResultType.FINISH && Race(it.raceId, it.goal, it.datetime).isBingo() }
              .filter { it.time != null }
        }

        aggregator = { s ->
          s.joinToString(", ") { it.time!!.standardFormat() }
              .let { "%user%'s best bingos: $it" }
        }
      }
}
