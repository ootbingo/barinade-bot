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
import java.time.Duration
import kotlin.time.toKotlinDuration

@LilyModule
class BingoStatModule(private val playerRepository: PlayerRepository) {

  @LilyCommand("average")
  fun average(command: Command): Answer<AnswerInfo> {

    val user = when (val messageInfo = command.messageInfo) {
      is DiscordMessageInfo -> messageInfo.message.author.name
      is IrcMessageInfo -> messageInfo.nick
      else -> ""
    }

    return Answer.ofText(if (user == "") "An error occurred finding the player."
                         else "The average of $user's last 10 races is: " +
        playerRepository.getPlayerByName(user)
            ?.races
            ?.asSequence()
            ?.sortedByDescending { it.recordDate }
            ?.take(10)
            ?.map { race -> race.raceResults.last { result -> result.player.name == user } }
            ?.map { it.time.seconds }
            ?.average()
            ?.let { Duration.ofSeconds(it.toLong()).standardFormat() })
  }
}
