package ootbingo.barinade.bot.misc

import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class HelpModuleTest : ModuleTest() {

  private val module = HelpModule()
  override val commands = mapOf(Pair("help", module::help))

  @Test
  internal fun answersWithHelpText() {

    val answer = whenDiscordMessageIsSent("", "!help")

    assertThat(answer?.text).isNotBlank()
  }
}
