package ootbingo.barinade.bot.modules

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
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

    val user = when (command.argumentCount) {

      0 -> when (val messageInfo = command.messageInfo) {
        is DiscordMessageInfo -> messageInfo.message.author.name
        is IrcMessageInfo -> messageInfo.nick
        else -> ""
      }

      1 -> command.getArgument(0)

      else -> ""
    }

    if (user == "") {
      return Answer.ofText("An error occurred finding the player.")
    }

    return Answer.ofText(playerRepository.getPlayerByName(user)
                             ?.let { "The average of $user's last 10 bingos is: ${average(it)}" }
                             ?: "User $user not found")
  }

  private fun average(player: Player): String {

    return player
        .races
        .asSequence()
        .filter { it.isBingo() }
        .sortedByDescending { it.recordDate }
        .take(10)
        .map { race -> race.raceResults.last { result -> result.player.name == player.name } }
        .map { it.time.seconds }
        .average()
        .let { Duration.ofSeconds(it.toLong()).standardFormat() }
  }
}
