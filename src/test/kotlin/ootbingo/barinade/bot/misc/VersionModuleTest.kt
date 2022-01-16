package ootbingo.barinade.bot.misc

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VersionModuleTest : ModuleTest() {

  private val properties = VersionProperties()
  private val module = VersionModule(properties)

  override val commands by lazy {
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
    thenAnswer = whenDiscordMessageIsSent("", this)!!
  }
}
