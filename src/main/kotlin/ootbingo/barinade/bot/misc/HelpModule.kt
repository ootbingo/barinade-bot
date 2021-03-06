package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.conditionalAnswer

@LilyModule
class HelpModule {

  @LilyCommand("help")
  fun help(command: Command): Answer<AnswerInfo>? =
      conditionalAnswer(command) {

        discordMessage = """
            The following commands are available:
            ```
            !average : Average time of your most recent bingos
            !median  : Median time of your most recent bingos
            !forfeits: Forfeit-ratio of your bingos
            
            !results : Most recent bingo times
            !best    : Best bingo times
            !racer   : Finished and forfeited bingos
            
            !balance : Balance up to 12 players into bingo teams
            !teamtime: Projected blackout time for a team
            
            !golds   : List Golden Rupee Chests
            !silvers : List Silver Rupee Rooms
            
            !shame   : Express your disapproval
            ```
          """.trimIndent()

        racetimeMessage =
            "Available commands: !average, !median, !forfeits, !results, !best, !racer, !golds, !silvers, !shame, !pick"
      }
}
