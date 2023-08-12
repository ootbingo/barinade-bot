package ootbingo.barinade.bot

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command

@LilyModule
class TestModule {

  private val password = "aslöjdf"

  @LilyCommand("average")
  fun average(chatCommand: Command): Answer<AnswerInfo>? {

    val map = mapOf("lsaf" to "asöjfh")

    val test = map["asf"]!!

    return Answer.ofText(password)
  }
}
