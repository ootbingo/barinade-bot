package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import ootbingo.barinade.bot.misc.ThemedWordService
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncRoomConfig
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncRoomConfig.Variant.*
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncService
import ootbingo.barinade.bot.racing_services.bingosync.bingosyncRoomConfig
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class LockoutRaceRoomTest {

  //<editor-fold desc="Setup">

  private val discordChannelMock = mock<TextChannel>()
  private val bingosyncServiceMock = mock<BingosyncService>()
  private val passwordSupplierMock = mock<ThemedWordService>()

  private val room = LockoutRaceRoom(
      mock(), discordChannelMock, mock(), mock(), mock(), bingosyncServiceMock, passwordSupplierMock,
  )

  private var returnedMessage: AtomicReference<String?>? = null

  @BeforeEach
  internal fun setup() {
    givenDiscordChannelName("")
    givenPasswordIsReturned("")
    givenServiceReturnsRoomUrl("")
  }

  //</editor-fold>

  @Test
  internal fun requestsNewBingosyncRoom() {

    val roomName = UUID.randomUUID().toString()
    val roomPassword = UUID.randomUUID().toString()

    givenDiscordChannelName(roomName)
    givenPasswordIsReturned(roomPassword)

    whenBingosyncRoomIsRequested()

    thenBingosyncRoomIsRequested(bingosyncRoomConfig {
      name = roomName
      password = roomPassword
      variant = BLACKOUT
      lockout = true
      seed = null
      hideCard = true
    })
  }

  @Test
  internal fun requestsPasswordWithMaxLengthSix() {

    whenBingosyncRoomIsRequested()

    thenPasswordRequestHasMaxLengthSix()
  }

  @Test
  internal fun messageContainsRoomLink() {

    val url = UUID.randomUUID().toString()

    givenServiceReturnsRoomUrl(url)

    whenBingosyncRoomIsRequested()

    thenMessageContains(url)
  }

  @Test
  internal fun messageContainsPassword() {

    val password = UUID.randomUUID().toString()

    givenPasswordIsReturned(password)

    whenBingosyncRoomIsRequested()

    thenMessageContains(password)
  }

  @Test
  internal fun onlyOpensOneRoom() {

    whenBingosyncRoomIsRequested()
    whenBingosyncRoomIsRequested()

    thenNoMessageIsSent()
    thenOneRoomWasCreated()
  }

  @Test
  internal fun postsErrorMessageIfRoomWasntOpened() {

    givenServiceReturnsRoomUrl(null)

    whenBingosyncRoomIsRequested()

    thenMessageContains("error")
  }

  //<editor-fold desc="Given">

  private fun givenDiscordChannelName(name: String) {
    whenever(discordChannelMock.name).thenReturn(name)
  }

  private fun givenPasswordIsReturned(password: String) {
    whenever(passwordSupplierMock.randomWord(any())).thenReturn(password)
  }

  private fun givenServiceReturnsRoomUrl(url: String?) {
    whenever(bingosyncServiceMock.openBingosyncRoom(any())).thenReturn(url)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenBingosyncRoomIsRequested() {
    returnedMessage = AtomicReference(room.bingosync(mock()))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenNoMessageIsSent() {
    assertThat(returnedMessage).hasValue(null)
  }

  private fun thenMessageContains(expectedPart: String) {
    assertThat(returnedMessage?.get()).containsIgnoringCase(expectedPart)
  }

  private fun thenBingosyncRoomIsRequested(expectedConfig: BingosyncRoomConfig) {
    verify(bingosyncServiceMock).openBingosyncRoom(expectedConfig)
  }

  private fun thenPasswordRequestHasMaxLengthSix() {
    verify(passwordSupplierMock).randomWord(6)
  }

  private fun thenOneRoomWasCreated() {
    verify(bingosyncServiceMock, times(1)).openBingosyncRoom(any())
  }

  //</editor-fold>
}
