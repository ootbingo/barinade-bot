package ootbingo.barinade.bot.discord

import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.newBingoRace
import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import java.util.*

class RaceRoomOpeningModuleTest : ModuleTest() {

  //<editor-fold desc="Setup">

  private val httpClientMock = mock<RacetimeHttpClient>()

  private val module = RaceRoomOpeningModule(httpClientMock)

  override val commands = mapOf(
    "newrace" to module::newRace,
    "newteamrace" to module::newTeamRace,
  )

  //</editor-fold>

  //<editor-fold desc="Test: !newrace">

  @Test
  internal fun opensSoloRace() {

    whenDiscordMessageIsSent("!newrace")

    thenRaceIsOpened(newBingoRace(false))
  }

  @Test
  internal fun returnsUrlOfSoloRace() {

    val url = UUID.randomUUID().toString()

    givenClientReturnsUrl(url)

    whenDiscordMessageIsSent("!newrace")

    thenUrlIsReturned(url)
  }

  @ParameterizedTest
  @EnumSource(MessageType::class)
  internal fun doesNotOpenSoloRaceIfMessageIsNotFromDiscord(type: MessageType) {

    whenMessageIsSent(type, "!newrace")

    thenNoRaceIsOpened()
  }

  //</editor-fold>

  //<editor-fold desc="Test: !newteamrace">

  @Test
  internal fun opensTeamRace() {

    whenDiscordMessageIsSent("!newteamrace")

    thenRaceIsOpened(newBingoRace(true))
  }

  @Test
  internal fun returnsUrlOfTeamRace() {

    val url = UUID.randomUUID().toString()

    givenClientReturnsUrl(url)

    whenDiscordMessageIsSent("!newteamrace")

    thenUrlIsReturned(url)
  }

  @ParameterizedTest
  @EnumSource(MessageType::class)
  internal fun doesNotOpenTeamRaceIfMessageIsNotFromDiscord(type: MessageType) {

    whenMessageIsSent(type, "!newteamrace")

    thenNoRaceIsOpened()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenClientReturnsUrl(url: String) {
    whenever(httpClientMock.startRace(any())).thenReturn(url)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenDiscordMessageIsSent(message: String) {
    whenDiscordMessageIsSent("", message)
  }

  private fun whenMessageIsSent(type: MessageType, message: String) {
    when (type) {
      MessageType.IRC -> whenIrcMessageIsSent("", message)
      MessageType.RACETIME -> whenRacetimeMessageIsSent("", message)
    }
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenRaceIsOpened(expectedRace: RacetimeEditableRace) {
    verify(httpClientMock).startRace(expectedRace)
  }

  private fun thenNoRaceIsOpened() {
    verifyNoInteractions(httpClientMock)
  }

  private fun thenUrlIsReturned(expectedUrl: String) {
    assertThat(answer).isEqualTo(expectedUrl)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  internal enum class MessageType { IRC, RACETIME }

  //</editor-fold>
}
