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
          # BingoBot Commands
          ## Statistics
          **!average** Average time of your most recent bingos
          **!median** Median time of your most recent bingos
          **!forfeits** Forfeit-ratio of your bingos
          **!results** Most recent bingo times
          **!best** Best bingo times
          **!racer** Finished and forfeited bingos
          ## Team Balancing
          **!balance** Balance up to 12 players into bingo teams
          **!teamtime** Projected blackout time for a team
          ## Game Info
          **!golds** List Golden Rupee Chests
          **!silvers** List Silver Rupee Rooms
          **!stalfos** List Stalfos locations
          ## Misc
          **!shame** Express your disapproval
          ## Racing
          **!newrace** Open a new Racetime room
          **!newteamrace** Open a new Racetime room for a team race
          **!lockout** Open a new lockout race room (only in #lockout-racing)
        """.trimIndent()

      racetimeMessage =
        "Available commands: !average, !median, !forfeits, !results, !best, !racer, !golds, !silvers, !stalfos, " +
            "!shame, !pick"
    }
}
