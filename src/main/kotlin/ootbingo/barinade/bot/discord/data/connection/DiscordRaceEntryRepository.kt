package ootbingo.barinade.bot.discord.data.connection

import ootbingo.barinade.bot.discord.data.model.DiscordPlayer
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntry
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntry.*
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface DiscordRaceEntryRepository : Repository<DiscordRaceEntry, EntryId> {

  fun save(entry: DiscordRaceEntry): DiscordRaceEntry
  fun saveAll(entries: Iterable<DiscordRaceEntry>): Iterable<DiscordRaceEntry>
  fun delete(entry: DiscordRaceEntry)

  @Query("select d from DiscordRaceEntry d where d.entryId.race.raceId = ?1 and d.entryId.player = ?2")
  fun findByRaceIdAndPlayer(raceId: Long, player: DiscordPlayer): DiscordRaceEntry?

  @Query("select d from DiscordRaceEntry d where d.entryId.race.raceId = ?1")
  fun findAllByRaceId(raceId: Long): Collection<DiscordRaceEntry>
}
