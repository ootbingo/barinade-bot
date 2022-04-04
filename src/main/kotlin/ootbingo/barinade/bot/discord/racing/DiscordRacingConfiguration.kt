package ootbingo.barinade.bot.discord.racing

import com.google.gson.Gson
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.connection.DiscordPlayerRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceEntryRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceRepository
import ootbingo.barinade.bot.discord.data.model.DiscordRaceType
import ootbingo.barinade.bot.misc.ThemedWordService
import ootbingo.barinade.bot.racing_services.bingosync.BingosyncService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal typealias WaitWrapper = Long.() -> Unit
internal typealias RaceRoomCommand = DiscordRaceRoom.(User) -> String?
internal typealias RaceStartExecutor = (() -> Unit) -> Unit

@Configuration
class DiscordRacingConfiguration(
    private val countdownService: CountdownService,
    private val waitWrapper: WaitWrapper,
    private val raceStartExecutor: RaceStartExecutor,
    private val bingosyncService: BingosyncService,
    private val passwordSupplierMock: ThemedWordService,
    private val playerRepository: DiscordPlayerRepository,
    private val raceRepository: DiscordRaceRepository,
    private val entryRepository: DiscordRaceEntryRepository,
    private val gson: Gson,
) {

  @Bean
  fun lockoutRoomFactory() = DiscordRaceRoomFactory {
    LockoutRaceRoom(
        DiscordRaceStatusHolder(playerRepository, raceRepository, entryRepository, gson, it, DiscordRaceType.LOCKOUT),
        it,
        raceStartExecutor,
        waitWrapper,
        countdownService,
        bingosyncService,
        passwordSupplierMock,
    )
  }
}