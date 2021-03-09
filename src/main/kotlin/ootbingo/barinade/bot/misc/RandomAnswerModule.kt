package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command

@LilyModule
class RandomAnswerModule(private val shameMessages: () -> List<String>) {

  private val shameList by lazy { shameMessages.invoke() }

  @LilyCommand("shame")
  fun shame(command: Command): Answer<AnswerInfo>? =
      Answer.ofText(randomValue(shameList))

  internal fun randomValue(values: Collection<String>?): String =
      values?.random() ?: ""
}
