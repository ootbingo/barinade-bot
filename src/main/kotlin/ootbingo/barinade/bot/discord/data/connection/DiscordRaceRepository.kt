package ootbingo.barinade.bot.discord.data.connection

import ootbingo.barinade.bot.discord.data.model.DiscordRace
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface DiscordRaceRepository : Repository<DiscordRace, Long> {

  fun save(race: DiscordRace): DiscordRace
  fun findById(id: Long): DiscordRace?
}
