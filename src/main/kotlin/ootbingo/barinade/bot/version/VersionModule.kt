package ootbingo.barinade.bot.version

import de.scaramanga.lily.core.annotations.LilyCommand
import de.scaramanga.lily.core.annotations.LilyModule
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command

@LilyModule
class VersionModule(private val versionProperties: VersionProperties) {

  @LilyCommand("version")
  fun buildInfo(command: Command): Answer<AnswerInfo>? {
    return Answer.ofText(versionProperties.let { "Version ${it.version} (${it.build})" })
  }
}
