package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState
import ootbingo.barinade.bot.discord.data.model.DiscordRaceState
import ootbingo.barinade.bot.extensions.exception
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.misc.ThemedWordService
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncRoomConfig.Variant.*
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncService
import ootbingo.barinade.bot.racing_services.bingosync.bingosyncRoomConfig
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

class LockoutRaceRoom(
    private val status: DiscordRaceStatusHolder,
    private val discordChannel: TextChannel,
    raceStartExecutor: (() -> Unit) -> Unit,
    wait: WaitWrapper,
    countdownService: CountdownService,
    private val bingosyncService: BingosyncService,
    private val passwordSupplier: ThemedWordService,
) : DiscordRaceRoom(status, discordChannel, raceStartExecutor, wait, countdownService) {

  private val logger = LoggerFactory.getLogger(LockoutRaceRoom::class.java)

  init {
    try {
      discordChannel.sendMessage(
          """
            Use the command `!bingosync` to open a room.
            **Update**: Use `!done` once you met any criteria that lets you win the race according to the tournament rules.
            ⚠️ It is not possible to undone yet! ⚠️
          """.trimIndent()
      ).queue()
    } catch (e: Exception) {
      logger.exception("Failed to send welcome message", e)
    }
  }

  private var roomCreated = false

  override fun done(entrant: User): String? {

    if (status.state != DiscordRaceState.PROGRESS) {
      return null
    }

    status.getStatusForEntrant(entrant) ?: return null

    val time = Duration.between(status.startTime, Instant.now())
    status.state = DiscordRaceState.FINISHED
    status.setStatusForAll(DiscordRaceEntryState.NOT_RANKED)
    status.setStatusForEntrant(entrant, DiscordRaceEntryState.FINISHED, time)

    return "${entrant.name} finished in ${time.standardFormat()}"
  }

  override fun bingosync(entrant: User): String? =
      bingosync()

  private fun bingosync(): String? {

    if (roomCreated) {
      return null
    }

    val password = passwordSupplier.randomWord(6) ?: "bingo"

    val url = bingosyncService.openBingosyncRoom(bingosyncRoomConfig {
      name = discordChannel.name
      this.password = password
      variant = BLACKOUT
      lockout = true
    }) ?: return "An error occurred trying to open the Bingosync room. Please try again in a few minutes."

    roomCreated = true

    try {
      status.addAdditionalInfo("bingosyncUrl", url)
      status.addAdditionalInfo("secret.bingosyncPassword", password)
    } catch (e: Exception) {
      logger.exception("Failed to save additional info", e)
    }

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
