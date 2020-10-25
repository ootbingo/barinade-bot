package ootbingo.barinade.bot.statistics

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.getUsername
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.ResultType

@LilyModule
class BingoHistoryModule(private val playerHelper: PlayerHelper) {

  @LilyCommand("results")
  fun results(command: Command): Answer<AnswerInfo>? {

    val username = command.messageInfo?.getUsername()!!
    val races = playerHelper.findResultsForPlayer(playerHelper.getPlayerByName(username)!!)
        .asSequence()
        .filter { it.resultType == ResultType.FINISH && Race(it.raceId, it.goal, it.datetime).isBingo() }
        .mapNotNull { it.time?.standardFormat() }
        .take(10)
        .joinToString(", ")

    return Answer.ofText("The last Bingos of $username: $races")
  }
}
