package ootbingo.barinade.bot.lockout

import ootbingo.barinade.bot.discord.DiscordChannelService
import ootbingo.barinade.bot.testutils.ModuleTest
import org.mockito.kotlin.mock

internal class LockoutModuleTest : ModuleTest() {

  //<editor-fold desc="Setup">

  private val properties = LockoutProperties()
  private val discordChannelServiceMock = mock<DiscordChannelService>()

  private val module = LockoutModule(properties, discordChannelServiceMock)

  override val commands by lazy {
    mapOf(
        "lockout" to module::lockout,
    )
  }

  //</editor-fold>
}
