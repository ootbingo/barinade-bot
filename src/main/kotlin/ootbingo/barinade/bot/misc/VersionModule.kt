package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command

@LilyModule
class VersionModule(private val versionProperties: VersionProperties) {

  @LilyCommand("version")
  fun buildInfo(command: Command): Answer<AnswerInfo>? {
    return Answer.ofText(versionProperties.let { "Version ${it.version} (${it.build})" })
  }
}
