package ootbingo.barinade.bot.statistics

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import ootbingo.barinade.bot.data.PlayerDao
import ootbingo.barinade.bot.extensions.getUsername
import ootbingo.barinade.bot.extensions.standardFormat

@LilyModule
class BingoHistoryModule(private val playerDaoMock: PlayerDao) {

  @LilyCommand("results")
  fun results(command: Command): Answer<AnswerInfo>? {

    val username = command.messageInfo?.getUsername()!!
    val races = playerDaoMock.findResultsForPlayer(username)
        .filter { !it.isForfeit && it.isBingo }
        .map { it.time.standardFormat() }
        .take(10)
        .joinToString(", ")

    return Answer.ofText("The last Bingos of $username: $races")
  }
}
