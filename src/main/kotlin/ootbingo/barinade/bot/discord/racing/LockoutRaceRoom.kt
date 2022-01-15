package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.extensions.exception
import ootbingo.barinade.bot.misc.ThemedWordService
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncRoomConfig.Variant.*
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncService
import ootbingo.barinade.bot.racing_services.bingosync.bingosyncRoomConfig
import org.slf4j.LoggerFactory

class LockoutRaceRoom(
    statusMock: DiscordRaceStatusHolder,
    private val discordChannel: TextChannel,
    raceStartExecutor: (() -> Unit) -> Unit,
    wait: WaitWrapper,
    countdownService: CountdownService,
    private val bingosyncService: BingosyncService,
    private val passwordSupplier: ThemedWordService,
) : DiscordRaceRoom(statusMock, discordChannel, raceStartExecutor, wait, countdownService) {

  private val logger = LoggerFactory.getLogger(LockoutRaceRoom::class.java)

  init {
    try {
      discordChannel.sendMessage("Use the command `!bingosync` to open a room.").queue()
    } catch (e: Exception) {
      logger.exception("Failed to send welcome message", e)
    }
  }

  var roomCreated = false

  override fun bingosync(entrant: User): String? =
      bingosync()

  private fun bingosync(): String? {

    if (roomCreated) {
      return null
    }

    val password = passwordSupplier.randomWord(6)

    val url = bingosyncService.openBingosyncRoom(bingosyncRoomConfig {
      name = discordChannel.name
      this.password = password
      variant = BLACKOUT
      lockout = true
    }) ?: return "An error occurred trying to open the Bingosync room. Please try again in a few minutes."

    roomCreated = true

    return """
      Password: **$password**
      $url
    """.trimIndent()
  }

  override fun readyToStart(): Boolean {

    if (!roomCreated) {
      bingosync()?.let { discordChannel.sendMessage(it).queue() }
    }

    return true
  }
}
