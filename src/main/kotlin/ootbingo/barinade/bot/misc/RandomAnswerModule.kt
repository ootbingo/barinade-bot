package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.getUsername
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo

@LilyModule
class RandomAnswerModule(private val shameMessages: () -> List<String>) {

  private val shameList by lazy { shameMessages.invoke() }

  private val rows =
      listOf("ROW1", "ROW2", "ROW3", "ROW4", "ROW5", "COL1", "COL2", "COL3", "COL4", "COL5", "TL-BR", "BL-TR")

  @LilyCommand("shame")
  fun shame(command: Command): Answer<AnswerInfo>? =
      Answer.ofText(randomValue(shameList).replace("<name>", command.messageInfo.getUsername() ?: "ERROR"))

  @LilyCommand("pick")
  fun pick(command: Command): Answer<AnswerInfo>? =
      command.messageInfo
          .takeIf { it is RacetimeMessageInfo }
          ?.let { it as RacetimeMessageInfo }
          ?.let { Answer.ofText("${it.message.user?.name}: Your row is ${randomValue(rows)}") }

  internal fun randomValue(values: Collection<String>?): String =
      values?.random() ?: ""
}
