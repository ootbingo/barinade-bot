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
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.model.Player
import ootbingo.barinade.bot.model.RaceResult
import java.time.Duration

@LilyModule
class BingoStatModule(private val playerRepository: PlayerRepository) {

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
          isBlank() -> "An error occurred finding the player."
          else -> "User $this not found"
        }
      })
    }
        ?: return Answer.ofText("An error occurred finding the player.")

    val average = average(queryInfo)

    return Answer
        .ofText("The average of ${queryInfo.player.name}'s last ${average.raceCount} bingos is: ${average.result} " +
                    "(Forfeits: ${average.forfeitsSkipped})")
  }

  private fun getRequesterQueryInfo(messageInfo: MessageInfo): QueryInfo? {

    val username = findUsername(messageInfo)

    return playerRepository.getPlayerByName(username)
        ?.let { QueryInfo(it) } ?: throw PlayerNotFoundException(username)
  }

  private fun getSingleArgumentQueryInfo(messageInfo: MessageInfo, arg0: String): QueryInfo? {

    val user = with(arg0) {
      when {
        matches(Regex("\\d+")) -> findUsername(messageInfo)
        else -> arg0
      }
    }

    val raceCount = with(arg0) {
      when {
        matches(Regex("\\d+")) -> this.toInt()
        else -> 10
      }
    }

    return playerRepository.getPlayerByName(user)
        ?.let { QueryInfo(it, raceCount) } ?: throw PlayerNotFoundException(user)
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

  private fun average(queryInfo: QueryInfo): ResultInfo {

    var forfeitsSkipped = 0

    val allBingos = queryInfo.player
        .races
        .asSequence()
        .filter { it.isBingo() }
        .sortedByDescending { it.recordDate }
        .toMutableList()

    val toAverage = mutableListOf<RaceResult>()

    while (toAverage.size < queryInfo.raceCount && allBingos.isNotEmpty()) {

      val result = allBingos.removeAt(0).raceResults.last { it.player.name == queryInfo.player.name }

      if (result.isForfeit()) {
        forfeitsSkipped++
      } else {
        toAverage.add(result)
      }
    }

    return toAverage
        .map { it.time.seconds }
        .average()
        .let { Duration.ofSeconds(it.toLong()).standardFormat() }
        .let { ResultInfo(it, toAverage.size, forfeitsSkipped) }
  }

  private fun findUsername(messageInfo: MessageInfo): String =
      when (messageInfo) {
        is DiscordMessageInfo -> messageInfo.message.author.name
        is IrcMessageInfo -> messageInfo.nick
        else -> ""
      }

  private inner class QueryInfo(val player: Player, val raceCount: Int = 10)
  private inner class ResultInfo(val result: String, val raceCount: Int, val forfeitsSkipped: Int)

  private inner class PlayerNotFoundException(val username: String) : Exception()
}
