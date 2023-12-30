package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.conditionalAnswer

@LilyModule
class InfoModule {

  @LilyCommand("golds", "gold", "goldrupees", "goldenrupees")
  fun golds(command: Command): Answer<AnswerInfo> =
      conditionalAnswer(command) {
        discordMessage = """
          ## Golden Rupee Locations
          * Dead Hand's Room in BotW
          * Kakariko Redead Grotto
          * SoS Grotto in DMT
          * Goron City Boulder Maze
          * GTG Like-Like Room
          * Fire Temple after Elevator
        """.trimIndent()

        racetimeMessage =
            "Golden rupees: BotW, Kakariko, DMT, Goron City, GTG, Fire Temple"
      }

  @LilyCommand("silvers", "silver", "silverrupees")
  fun silvers(command: Command): Answer<AnswerInfo> =
      conditionalAnswer(command) {
        discordMessage = """
          ## Silver Rupee Locations
          * Shadow Temple 1st Small Key Room
          * Shadow Temple before Falling Spikes
          * Shadow Temple before Urn Key
          * Ice Cavern Turning Guillotine
          * Ice Cavern Block Puzzle Room
          * Spirit Temple Adult Side Boulder Room
          * Spirit Temple Child Side first Small Key
          * Spirit Temple Child Side Sun Block Room
          * BotW Basement
          * GTG Boulder maze
          * GTG Lava Room
          * GTG Under Water
          * Spirit Trial
          * Forest Trial
          * Fire Trial
          * Light Trial
        """.trimIndent()

        racetimeMessage =
            "Silver rupees: BotW, 2 x Ice Cavern, 3 x GTG, 3 x Shadow Temple, 3 x Spirit Temple, 4 x Trials"
      }

  @LilyCommand("stalfos")
  fun stalfos(command: Command): Answer<AnswerInfo> =
      conditionalAnswer(command) {
        discordMessage = """
          ## Stalfos Locations
          * Forest Temple Stalfos Small Key Room (x2)
          * Forest Temple Bow Room (x3)
          * Shadow Temple Cavernous Room (x1)
          * Shadow Temple Boat Ride (x2)
          * GTG Stalfos Room (x2)
          * Spirit Temple Child Side Green Bubble Room (x1)
          * Ganon's Tower Boss Key Room (x2)
          * Ganon's Tower Collapse (x2)
        """.trimIndent()

        racetimeMessage = "5 x Forest Temple, 3 x Shadow Temple, 2 x GTG, 1 x Spirit Temple, 2 x Ganon’s Tower, " +
            "2 x Ganon’s Tower Collapse"
      }
}
