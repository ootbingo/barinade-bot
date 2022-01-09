package ootbingo.barinade.bot.misc

import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class InfoModuleTest : ModuleTest() {

  private val module = InfoModule()

  override val commands = mapOf("golds" to module::golds, "silvers" to module::silvers)

  //<editor-fold desc="!golds">

  @Test
  internal fun goldsMultilineOnDiscord() {
    whenDiscordMessageIsSent("", "!golds")
    thenAnswerIsPreformatted()
  }

  @Test
  internal fun goldsSingleLineOnRacetime() {
    whenRacetimeMessageIsSent("", "!golds")
    thenAnswerIsSingleLine()
  }

  //</editor-fold>

  //<editor-fold desc="!silvers">

  @Test
  internal fun silversMultilineOnDiscord() {
    whenDiscordMessageIsSent("", "!silvers")
    thenAnswerIsPreformatted()
  }

  @Test
  internal fun silversSingleLineOnRacetime() {
    whenRacetimeMessageIsSent("", "!silvers")
    thenAnswerIsSingleLine()
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenAnswerIsPreformatted() {
    assertThat(answer).startsWith("```")
    assertThat(answer).endsWith("```")
  }

  private fun thenAnswerIsSingleLine() {
    assertThat(answer).doesNotStartWith("```")
    assertThat(answer).doesNotContain("\n")
  }

  //</editor-fold>
}
