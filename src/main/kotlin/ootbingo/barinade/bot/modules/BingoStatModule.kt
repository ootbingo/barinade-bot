package ootbingo.barinade.bot.modules

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.discord.connection.DiscordMessageInfo
import de.scaramanga.lily.irc.connection.IrcMessageInfo
import ootbingo.barinade.bot.data.PlayerRepository
import ootbingo.barinade.bot.extensions.median
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.model.Player
import ootbingo.barinade.bot.model.Race
import java.time.Duration

@LilyModule
class BingoStatModule(private val playerRepository: PlayerRepository) {

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

    return Answer
        .ofText("The average of ${queryInfo.player.name}'s last ${average.raceCount} bingos is: ${average.result} " +
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

    return Answer
        .ofText("The median of ${queryInfo.player.name}'s last ${median.raceCount} bingos is: ${median.result} " +
                    "(Forfeits: ${median.forfeitsSkipped})")
  }

  private fun getRequesterQueryInfo(messageInfo: MessageInfo, raceCount: Int = 10): QueryInfo? {

    val username = findUsername(messageInfo)

    return playerRepository.getPlayerByName(username)
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

    return playerRepository.getPlayerByName(user)
        ?.let { QueryInfo(it, parsedRaceCount) } ?: throw PlayerNotFoundException(user)
  }

  private fun getDoubleArgumentQueryInfo(arg0: String, arg1: String): QueryInfo? {

    val args = listOf(arg0, arg1)

    val raceCount = args.firstOrNull { it.matches(Regex("\\d+")) }?.toInt()
    val user = args.firstOrNull { !it.matches(Regex("\\d+")) }

    if (raceCount == null || user == null) {
      return null
    }

    return playerRepository.getPlayerByName(user)
        ?.let { QueryInfo(it, raceCount) } ?: throw PlayerNotFoundException(user)
  }

  private fun allRacesForComputation(queryInfo: QueryInfo): ComputationRaces {

    var forfeitsSkipped = 0

    val allBingos = queryInfo.player
        .races
        .asSequence()
        .filter { it.isBingo() }
        .sortedByDescending { it.recordDate }
        .toMutableList()

    val toAverage = mutableListOf<Race>()

    while (toAverage.size < queryInfo.raceCount && allBingos.isNotEmpty()) {

      val bingo = allBingos.removeAt(0)
      val result = bingo.raceResults.last { it.player.name == queryInfo.player.name }

      if (result.isForfeit()) {
        forfeitsSkipped++
      } else {
        toAverage.add(bingo)
      }
    }

    return ComputationRaces(toAverage, forfeitsSkipped)
  }

  private fun average(queryInfo: QueryInfo): ResultInfo {

    val toAverage = allRacesForComputation(queryInfo)

    return toAverage.races
        .map { it.raceResults.last { result -> result.player.name == queryInfo.player.name } }
        .map { it.time.seconds }
        .average()
        .let { Duration.ofSeconds(it.toLong()).standardFormat() }
        .let { ResultInfo(it, toAverage.races.size, toAverage.forfeitsSkipped) }
  }

  private fun median(queryInfo: QueryInfo): ResultInfo {

    val toAverage = allRacesForComputation(queryInfo)

    return toAverage.races
        .map { it.raceResults.last { result -> result.player.name == queryInfo.player.name } }
        .map { it.time.seconds }
        .median()
        .let { Duration.ofSeconds(it).standardFormat() }
        .let { ResultInfo(it, toAverage.races.size, toAverage.forfeitsSkipped) }
  }

  private fun findUsername(messageInfo: MessageInfo): String =
      when (messageInfo) {
        is DiscordMessageInfo -> messageInfo.message.author.name
        is IrcMessageInfo -> messageInfo.nick
        else -> ""
      }

  private inner class QueryInfo(val player: Player, val raceCount: Int)
  private inner class ResultInfo(val result: String, val raceCount: Int, val forfeitsSkipped: Int)
  private inner class ComputationRaces(val races: List<Race>, val forfeitsSkipped: Int)

  private inner class PlayerNotFoundException(val username: String) : Exception()
}
