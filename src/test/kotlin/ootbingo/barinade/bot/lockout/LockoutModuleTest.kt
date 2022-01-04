package ootbingo.barinade.bot.lockout

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import ootbingo.barinade.bot.discord.DiscordChannelService
import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*

internal class LockoutModuleTest : ModuleTest() {

  //<editor-fold desc="Setup">

  private val defaultChannelId = "lockout_channel"
  private val defaultCategoryId = "lockout_category"

  private val properties = LockoutProperties()
  private val discordChannelServiceMock = mock<DiscordChannelService>()

  private val module = LockoutModule(properties, discordChannelServiceMock)

  override val commands by lazy {
    mapOf(
        "lockout" to module::lockout,
    )
  }

  private var mockChannelId = ""
  private val channelMock = mock<TextChannel>()
  private val guildMock = mock<Guild>()

  @BeforeEach
  internal fun setup() {

    properties.discordChannel = defaultChannelId
    properties.discordCategory = defaultCategoryId

    doAnswer { mockChannelId }.whenever(channelMock).id
    whenever(channelMock.guild).thenReturn(guildMock)
  }

  //</editor-fold>

  @Test
  internal fun opensNewLockoutRoom() {

    val channelId = UUID.randomUUID().toString()
    val categoryId = UUID.randomUUID().toString()

    givenMockChannelId(channelId)
    givenLockoutChannelId(channelId)
    givenLockoutCategoryId(categoryId)

    whenDiscordMessageIsSent("test", "!lockout", channelMock)

    thenChannelCreationIsRequested(categoryId, guildMock, Regex("""lockout-[0-9a-z]{8}"""))
  }

  @Test
  internal fun linksNewChannelInAnswer() {

    val newChannelHash = UUID.randomUUID().toString()

    givenMockChannelId(defaultChannelId)
    givenDiscordServiceReturnsChannel(mock<TextChannel>().apply { whenever(this.asMention).thenReturn(newChannelHash) })

    whenDiscordMessageIsSent("test", "!lockout", channelMock)

    thenAnswerContains(newChannelHash)
  }

  @Test
  internal fun doesNotCreateChannelIfMessageWasSentFromWrongChannel() {

    givenMockChannelId("different")

    whenDiscordMessageIsSent("user", "!lockout", channelMock)

    thenNoChannelIsCreated()
  }

  @Test
  internal fun answersIfErrorOccurs() {

    givenMockChannelId(defaultChannelId)
    givenChannelCreationFails()

    whenDiscordMessageIsSent("test", "!lockout", channelMock)

    thenAnswerContains("error")
  }

  //<editor-fold desc="Given">

  private fun givenMockChannelId(id: String) {
    mockChannelId = id
  }

  private fun givenLockoutChannelId(id: String) {
    properties.discordChannel = id
  }

  private fun givenLockoutCategoryId(id: String) {
    properties.discordCategory = id
  }

  private fun givenDiscordServiceReturnsChannel(channel: TextChannel) =
      whenever(discordChannelServiceMock.createChannel(any())).thenReturn(channel)

  private fun givenChannelCreationFails() =
      whenever(discordChannelServiceMock.createChannel(any())).thenThrow(RuntimeException())

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenChannelCreationIsRequested(expectedCategoryId: String, expectedGuild: Guild, nameMatches: Regex) {

    val captor = argumentCaptor<DiscordChannelService.ChannelCreationBuilder.() -> Unit>()
    verify(discordChannelServiceMock).createChannel(captor.capture())
    val builder = DiscordChannelService.ChannelCreationBuilder().apply(captor.lastValue)

    assertThat(builder.categoryId).isEqualTo(expectedCategoryId)
    assertThat(builder.guild).isEqualTo(expectedGuild)
    assertThat(builder.name).matches(nameMatches.toPattern())
  }

  private fun thenNoChannelIsCreated() = verifyNoInteractions(discordChannelServiceMock)

  private fun thenAnswerContains(expectedPart: String) {
    assertThat(answer).contains(expectedPart)
  }

  //</editor-fold>
}
