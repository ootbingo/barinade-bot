package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel

class LockoutRaceRoom(
    private val statusMock: DiscordRaceStatusHolder,
    discordChannel: TextChannel,
    raceStartExecutor: (() -> Unit) -> Unit,
    wait: WaitWrapper,
    countdownService: CountdownService,
) : DiscordRaceRoom(statusMock, discordChannel, raceStartExecutor, wait, countdownService) {

  override fun readyToStart(): Boolean {
    // TODO
    return true
  }
}
