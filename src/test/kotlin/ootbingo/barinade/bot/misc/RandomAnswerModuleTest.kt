package ootbingo.barinade.bot.misc

import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*
import java.util.stream.IntStream

internal class RandomAnswerModuleTest : ModuleTest() {

  //<editor-fold desc="Setup">

  private lateinit var shameList: List<String>
  private val module = spy(RandomAnswerModule { shameList })

  override val commands = mapOf("shame" to module::shame, "pick" to module::pick)

  //</editor-fold>

  //<editor-fold desc="Meta Function">

  @Test
  internal fun metaFunctionReturnsRandomValueFromList() {

    val values = IntStream.range(0, 20).mapToObj { UUID.randomUUID().toString() }.toList()

    val answers = values.map { module.randomValue(values) }

    answers.forEach {
      assertThat(it).isIn(values)
    }

    assertThat(answers.toSet()).hasSizeGreaterThan(1)
  }

  //</editor-fold>

  //<editor-fold desc="!shame">

  @Test
  internal fun shameCallsMetaFunctionWithShameList() {

    val shame1 = UUID.randomUUID().toString()
    val shame2 = UUID.randomUUID().toString()
    val shame3 = UUID.randomUUID().toString()

    givenShameList(shame1, shame2, shame3)

    whenDiscordMessageIsSent("", "!shame")

    thenMetaFunctionIsCalledWith(shame1, shame2, shame3)
  }

  @Test
  internal fun shameReturnsResultOfMetaFunctionDiscord() {

    val answer = UUID.randomUUID().toString()

    givenMetaFunctionReturns(answer)
    givenShameList("")

    whenDiscordMessageIsSent("", "!shame")

    thenAnswerMatches(answer)
  }

  @Test
  internal fun shameReturnsResultOfMetaFunctionRacetime() {

    val answer = UUID.randomUUID().toString()

    givenMetaFunctionReturns(answer)
    givenShameList("")

    whenRacetimeMessageIsSent("", "!shame")

    thenAnswerMatches(answer)
  }

  @Test
  internal fun replacesPlayerNameInShameMessage() {

    val player = UUID.randomUUID().toString()

    givenMetaFunctionReturns("<name> sucks!")
    givenShameList("")

    whenRacetimeMessageIsSent(player, "!shame")

    thenAnswerMatches("$player sucks!")
  }

  //</editor-fold>

  //<editor-fold desc="!pick">

  @Test
  internal fun pickCallsMetaFunction() {

    whenRacetimeMessageIsSent("any", "!pick")

    thenMetaFunctionIsCalledWith(
        "ROW1", "ROW2", "ROW3", "ROW4", "ROW5", "COL1", "COL2", "COL3", "COL4", "COL5", "TL-BR", "BL-TR"
    )
  }

  @Test
  internal fun pickReturnsRandomRowAndUsername() {

    val row = UUID.randomUUID().toString()
    val username = UUID.randomUUID().toString()

    givenMetaFunctionReturns(row)

    whenRacetimeMessageIsSent(username, "!pick")

    thenAnswerMatches(Regex("""$username:.*$row"""))
  }

  @Test
  internal fun pickDoesNotReturnOnDiscord() {

    whenDiscordMessageIsSent("any", "!pick")

    thenNoAnswerIsSent()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenShameList(vararg messages: String) {
    shameList = messages.toList()
  }

  private fun givenMetaFunctionReturns(random: String) {
    whenever(module.randomValue(any())).thenReturn(random)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenMetaFunctionIsCalledWith(vararg expectedValues: String) {
    val captor = argumentCaptor<Collection<String>>()
    verify(module).randomValue(captor.capture())
    assertThat(captor.lastValue).containsExactlyInAnyOrderElementsOf(expectedValues.toList())
  }

  private fun thenAnswerMatches(expectedText: String) {
    assertThat(answer).isEqualTo(expectedText)
  }

  private fun thenAnswerMatches(regex: Regex) {
    assertThat(answer).matches(regex.toPattern())
  }

  private fun thenNoAnswerIsSent() {
    assertThat(answer).isNull()
  }

  //</editor-fold>
}
