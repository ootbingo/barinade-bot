package ootbingo.barinade.bot.statistics

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.discord.connection.DiscordMessageInfo
import de.scaramanga.lily.irc.connection.IrcMessageInfo
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import ootbingo.barinade.bot.extensions.median
import ootbingo.barinade.bot.extensions.standardFormat
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Duration
import java.util.Locale

@LilyModule
class BingoStatModule(private val playerHelper: PlayerHelper) {

  private val logger = LoggerFactory.getLogger(BingoStatModule::class.java)

  private val errorMessage = "An error occurred finding the player."

  @LilyCommand("average")
  fun average(command: Command): Answer<AnswerInfo>? {

    val queryInfo = try {
      when (command.argumentCount) {
        0 -> getRequesterQueryInfo(command.messageInfo)
        1 -> getSingleArgumentQueryInfo(command.messageInfo, command.getArgument(0))
        2 -> getDoubleArgumentQueryInfo(command.getArgument(0), command.getArgument(1))
        else -> return null
      }
    } catch (e: PlayerNotFoundException) {
      return Answer.ofText(with(e.username) {
        when {
          isBlank() -> errorMessage
          else -> "User $this not found"
        }
      })
    }
        ?: return Answer.ofText(errorMessage)

    val average = average(queryInfo)

    if (average.raceCount == 0) {
      return Answer.ofText("${queryInfo.player.srlName} has not finished any bingos")
    }

    return Answer
        .ofText("The average of ${queryInfo.player.srlName}'s last ${average.raceCount} bingos is: ${average.result} " +
                    "(Forfeits: ${average.forfeitsSkipped})")
  }

  @LilyCommand("median")
  fun median(command: Command): Answer<AnswerInfo>? {

    val queryInfo = try {
      when (command.argumentCount) {
        0 -> getRequesterQueryInfo(command.messageInfo, 15)
        1 -> getSingleArgumentQueryInfo(command.messageInfo, command.getArgument(0), 15)
        2 -> getDoubleArgumentQueryInfo(command.getArgument(0), command.getArgument(1))
        else -> return null
      }
    } catch (e: PlayerNotFoundException) {
      return Answer.ofText(with(e.username) {
        when {
          isBlank() -> errorMessage
          else -> "User $this not found"
        }
      })
    }
        ?: return Answer.ofText(errorMessage)

    val median = median(queryInfo)

    if (median.raceCount == 0) {
      return Answer.ofText("${queryInfo.player.srlName} has not finished any bingos")
    }

    return Answer
        .ofText("The median of ${queryInfo.player.srlName}'s last ${median.raceCount} bingos is: ${median.result} " +
                    "(Forfeits: ${median.forfeitsSkipped})")
  }

  @LilyCommand("forfeits")
  fun forfeitRatio(command: Command): Answer<AnswerInfo>? {

    val username = when (command.argumentCount) {
      0 -> findUsername(command.messageInfo)
      1 -> command.getArgument(0)
      else -> ""
    }

    if (username.isBlank()) {
      return Answer.ofText(errorMessage)
    }

    val player = playerHelper.getPlayerByName(username) ?: return Answer.ofText("User $username not found")

    val bingos = playerHelper.findResultsForPlayer(player)
        .filter { Race(it.raceId, it.goal, it.datetime).isBingo() }

    if (bingos.isEmpty()) {
      return Answer.ofText("$username has not finished any bingos")
    }

    return Answer.ofText(
        bingos
            .filter { it.resultType != ResultType.FINISH }
            .count()
            .toDouble()
            .let { 100 * it / bingos.count().toDouble() }
            .let { DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH)).format(it) }
            .let { "The forfeit ratio of $username is: $it%" })
  }

  private fun getRequesterQueryInfo(messageInfo: MessageInfo, raceCount: Int = 10): QueryInfo? {

    val username = findUsername(messageInfo)

    return playerHelper.getPlayerByName(username)
        ?.let { QueryInfo(it, raceCount) } ?: throw PlayerNotFoundException(username)
  }

  private fun getSingleArgumentQueryInfo(messageInfo: MessageInfo, arg0: String, raceCount: Int = 10): QueryInfo? {

    val user = with(arg0) {
      when {
        matches(Regex("\\d+")) -> findUsername(messageInfo)
        else -> arg0
      }
    }

    val parsedRaceCount = with(arg0) {
      when {
        matches(Regex("\\d+")) -> this.toInt()
        else -> raceCount
      }
    }

    return playerHelper.getPlayerByName(user)
        ?.let { QueryInfo(it, parsedRaceCount) } ?: throw PlayerNotFoundException(user)
  }

  private fun getDoubleArgumentQueryInfo(arg0: String, arg1: String): QueryInfo? {

    val args = listOf(arg0, arg1)

    val raceCount = args.firstOrNull { it.matches(Regex("\\d+")) }?.toInt()
    val user = args.firstOrNull { !it.matches(Regex("\\d+")) }

    if (raceCount == null || user == null) {
      return null
    }

    return playerHelper.getPlayerByName(user)
        ?.let { QueryInfo(it, raceCount) } ?: throw PlayerNotFoundException(user)
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

    return ComputationRaces(toAverage, forfeitsSkipped)
  }

  private fun average(queryInfo: QueryInfo): ComputationResult {

    val toAverage = allRacesForComputation(queryInfo)

    return toAverage.races
        .map {
          it.time?.seconds?: logMissingResultTime(it.raceId)
        }
        .average()
        .let { Duration.ofSeconds(it.toLong()).standardFormat() }
        .let { ComputationResult(it, toAverage.races.size, toAverage.forfeitsSkipped) }
  }

  private fun median(queryInfo: QueryInfo): ComputationResult {

    val toMedian = allRacesForComputation(queryInfo)

    if (toMedian.races.isEmpty()) {
      return ComputationResult("", 0, 0)
    }

    return toMedian.races
        .map {
          it.time?.seconds?: logMissingResultTime(it.raceId)
        }
        .median()
        .let { Duration.ofSeconds(it).standardFormat() }
        .let { ComputationResult(it, toMedian.races.size, toMedian.forfeitsSkipped) }
  }

  private fun findUsername(messageInfo: MessageInfo): String =
      when (messageInfo) {
        is DiscordMessageInfo -> messageInfo.message.author.name
        is IrcMessageInfo -> messageInfo.nick
        else -> ""
      }

  fun median(username: String): Duration? {

    val player = playerHelper.getPlayerByName(username)
    return player
        ?.let { allRacesForComputation(QueryInfo(it, 15)) }
        ?.races
        ?.map {
          it.time?.seconds?: logMissingResultTime(it.raceId)
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
  private inner class ComputationResult(val result: String, val raceCount: Int, val forfeitsSkipped: Int)
  private inner class ComputationRaces(val races: List<ResultInfo>, val forfeitsSkipped: Int)

  private inner class PlayerNotFoundException(val username: String) : Exception()
}
