package ootbingo.barinade.bot.discord.racing

import ootbingo.barinade.bot.misc.ThemedWordService
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncService
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
    private val bingosyncService: BingosyncService,
    private val passwordSupplierMock: ThemedWordService,
) {

  @Bean
  fun lockoutRoomFactory() = DiscordRaceRoomFactory {
    LockoutRaceRoom(
        DiscordRaceStatusHolder(),
        it,
        raceStartExecutor,
        waitWrapper,
        countdownService,
        bingosyncService,
        passwordSupplierMock,
    )
  }
}
