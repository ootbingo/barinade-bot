package ootbingo.barinade.bot.statistics

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.median
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Duration
import java.util.Locale

@LilyModule
class BingoStatModule(private val playerHelper: PlayerHelper) {

  private val logger = LoggerFactory.getLogger(BingoStatModule::class.java)

  @LilyCommand("average")
  fun average(chatCommand: Command): Answer<AnswerInfo>? =
      playerHelper.query {

        command = chatCommand

        defaultAmount = 10

        var forfeits = 0

        raceFilter = {
          it
              .filter { r ->
                if (r.resultType == ResultType.FORFEIT) {
                  forfeits++
                  false
                } else {
                  true
                }
              }
        }

        aggregator = {
          it.map { r -> r.time!!.toSeconds() }
              .average()
              .let { avg ->
                "The average of %user%'s last %count% bingos is: " +
                    "${Duration.ofSeconds(avg.toLong()).standardFormat()} (Forfeits: $forfeits)"
              }
        }
      }

  @LilyCommand("median")
  fun median(chatCommand: Command): Answer<AnswerInfo>? =
      playerHelper.query {

        command = chatCommand

        defaultAmount = 15

        var forfeits = 0

        raceFilter = {
          it
              .filter { r ->
                if (r.resultType == ResultType.FORFEIT) {
                  forfeits++
                  false
                } else {
                  true
                }
              }
        }

        aggregator = {
          it.map { r -> r.time!!.toSeconds() }
              .median()
              .let { avg ->
                "The median of %user%'s last %count% bingos is: " +
                    "${Duration.ofSeconds(avg).standardFormat()} (Forfeits: $forfeits)"
              }
        }
      }

  @LilyCommand("forfeits")
  fun forfeitRatio(chatCommand: Command): Answer<AnswerInfo>? =
      playerHelper.query {

        command = chatCommand

        allowDifferentAmounts = false

        aggregator = {

          val total = it.count().toDouble()
          val forfeits = it.filter { r -> r.resultType == ResultType.FORFEIT }.count().toDouble()

          (100 * forfeits / total)
              .let { fr -> DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH)).format(fr) }
              .let { fr -> "The forfeit ratio of %user% is: $fr%" }
        }
      }

  private fun allRacesForComputation(queryInfo: QueryInfo): ComputationRaces {

    var forfeitsSkipped = 0

    val allBingos = queryInfo.player
        .let { playerHelper.findResultsForPlayer(it) }
        .asSequence()
        .filter { Race(it.raceId, it.goal, it.datetime).isBingo() }
        .toMutableList()

    val toAverage = mutableListOf<ResultInfo>()

    while (toAverage.size < queryInfo.raceCount && allBingos.isNotEmpty()) {

      val bingo = allBingos.removeAt(0)

      if (bingo.resultType != ResultType.FINISH) {
        forfeitsSkipped++
      } else {
        toAverage.add(bingo)
      }
    }

    return ComputationRaces(toAverage)
  }

  fun median(username: String): Duration? {

    val player = playerHelper.getPlayerByName(username)
    return player
        ?.let { allRacesForComputation(QueryInfo(it, 15)) }
        ?.races
        ?.map {
          it.time?.seconds ?: logMissingResultTime(it.raceId)
        }
        ?.let { if (it.isEmpty()) return null else it }
        ?.median()
        ?.let { Duration.ofSeconds(it) }
  }

  fun forfeitRatio(username: String): Double? {

    val player = playerHelper.getPlayerByName(username)

    val allBingos = player?.let { playerHelper.findResultsForPlayer(it) }
        ?.filter { Race(it.raceId, it.goal, it.datetime).isBingo() }

    if (allBingos.isNullOrEmpty()) {
      return null
    }

    return allBingos
        .filter { it.resultType != ResultType.FINISH }
        .count()
        .toDouble()
        .let { it / allBingos.count() }
  }

  private fun logMissingResultTime(raceId: String): Long {
    logger.error("Finished race without time: {}", raceId)
    return -1L
  }

  private inner class QueryInfo(val player: Player, val raceCount: Int)
  private inner class ComputationRaces(val races: List<ResultInfo>)
}
