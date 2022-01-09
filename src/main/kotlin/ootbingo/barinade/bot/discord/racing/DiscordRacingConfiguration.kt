package ootbingo.barinade.bot.discord.racing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal typealias WaitWrapper = Long.() -> Unit
internal typealias RaceRoomCommand = DiscordRaceRoom.(DiscordEntrant) -> String?
internal typealias RaceStartExecutor = (() -> Unit) -> Unit

@Configuration
class DiscordRacingConfiguration(
    private val countdownService: CountdownService,
    private val waitWrapper: WaitWrapper,
    private val raceStartExecutor: RaceStartExecutor,
) {

  @Bean
  fun lockoutRoomFactory() = DiscordRaceRoomFactory {
    LockoutRaceRoom(
        DiscordRaceStatusHolder(),
        it,
        raceStartExecutor,
        waitWrapper,
        countdownService,
    )
  }
}
