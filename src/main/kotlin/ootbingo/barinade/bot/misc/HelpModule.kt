package ootbingo.barinade.bot.misc

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command

@LilyModule
class HelpModule {

  @LilyCommand("help")
  fun help(command: Command): Answer<AnswerInfo>? =
      Answer.ofText(
          """
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
            ```
          """.trimIndent()
      )
}
