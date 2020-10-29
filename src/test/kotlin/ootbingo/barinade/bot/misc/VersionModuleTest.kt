package ootbingo.barinade.bot.misc

import com.nhaarman.mockitokotlin2.mock
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.discord.connection.DiscordMessageInfo
import net.dv8tion.jda.api.entities.Message
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VersionModuleTest {

  private val properties = VersionProperties()
  private val module = VersionModule(properties)

  private val commands by lazy {
    mapOf(Pair("version", module::buildInfo))
  }

  private lateinit var thenAnswer: Answer<AnswerInfo>

  @Test
  internal fun answersWithBuildInfoToDiscordMessage() {

    givenProperties {
      version = "42.123.0-TEST"
      build = "345agf"
    }

    "!version".sendAsDiscordMessage()

    thenAnswer hasVersion "42.123.0-TEST"
    thenAnswer hasBuild "345agf"
  }

  private fun givenProperties(block: VersionProperties.() -> Unit) {
    properties.apply(block)
  }

  private infix fun Answer<AnswerInfo>.hasVersion(version: String) {
    val versionPart = this.text.split(" ")[1]
    assertThat(versionPart).isEqualTo(version)
  }

  private infix fun Answer<AnswerInfo>.hasBuild(build: String) {
    val buildPart = this.text.split("(")[1].replace(")", "")
    assertThat(buildPart).isEqualTo(build)
  }

  private fun String.sendAsDiscordMessage() {

    val discordMessageMock = mock<Message>()
    thenAnswer = whenMessageIsSent(this, DiscordMessageInfo.withMessage(discordMessageMock))!!
  }

  private fun whenMessageIsSent(message: String, messageInfo: MessageInfo): Answer<AnswerInfo>? {

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    return commands.getValue(command).invoke(generateCommand(message, messageInfo))
  }

  private fun generateCommand(message: String, messageInfo: MessageInfo): Command {

    val parts = message.substring(1).split(" ")

    return object : Command {
      override fun getMessageInfo(): MessageInfo {
        return messageInfo
      }

      override fun getArgument(n: Int): String {
        return parts[n + 1]
      }

      override fun getName(): String {
        return parts[0]
      }

      override fun getArgumentCount(): Int {
        return parts.size - 1
      }
    }
  }
}
