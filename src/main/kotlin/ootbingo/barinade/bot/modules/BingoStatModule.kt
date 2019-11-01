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
import java.time.Duration

@LilyModule
class BingoStatModule(private val playerRepository: PlayerRepository) {

  @LilyCommand("average")
  fun average(command: Command): Answer<AnswerInfo> {

    return when (command.argumentCount) {
      0 -> printAverage(command.messageInfo)
      1 -> printAverage(command.messageInfo, command.getArgument(0))
      else -> Answer.ofText("")
    }
  }

  private fun printAverage(messageInfo: MessageInfo): Answer<AnswerInfo> {

    val user = findUsername(messageInfo)

    if (user == "") {
      return Answer.ofText("An error occurred finding the player.")
    }

    return Answer.ofText(playerRepository.getPlayerByName(user)
                             ?.let { "The average of $user's last 10 bingos is: ${average(it)}" }
                             ?: "User $user not found")
  }

  private fun printAverage(messageInfo: MessageInfo, arg0: String): Answer<AnswerInfo> {

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

    if (user == "") {
      return Answer.ofText("An error occurred finding the player.")
    }

    return Answer.ofText(playerRepository.getPlayerByName(user)
                             ?.let { "The average of $user's last $raceCount bingos is: ${average(it, raceCount)}" }
                             ?: "User $user not found")
  }

  private fun average(player: Player, raceCount: Int = 10): String {

    return player
        .races
        .asSequence()
        .filter { it.isBingo() }
        .sortedByDescending { it.recordDate }
        .take(raceCount)
        .map { race -> race.raceResults.last { result -> result.player.name == player.name } }
        .map { it.time.seconds }
        .average()
        .let { Duration.ofSeconds(it.toLong()).standardFormat() }
  }

  private fun findUsername(messageInfo: MessageInfo): String =
      when (messageInfo) {
        is DiscordMessageInfo -> messageInfo.message.author.name
        is IrcMessageInfo -> messageInfo.nick
        else -> ""
      }
}
