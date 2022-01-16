package ootbingo.barinade.bot.discord.data.model

import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.model.DiscordRaceType.*
import java.time.Instant
import javax.persistence.*

@Entity
class DiscordRace(

    @Id
    var raceId: Long = 0,
    var name: String = "",
    @Enumerated(EnumType.STRING)
    var type: DiscordRaceType = GENERIC,
    var openingTime: Instant = Instant.now(),
    var startTime: Instant? = null,
    var endTime: Instant? = null,
    @Enumerated(EnumType.STRING)
    var state: DiscordRaceState = DiscordRaceState.OPEN,
    @OneToMany(cascade = [], mappedBy = "entryId.race", fetch = FetchType.LAZY)
    var entrants: List<DiscordRaceEntry> = mutableListOf(),
    @Lob
    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.LAZY)
    var additionalInfo: String = "",
) {

  fun entryOfUser(user: User): DiscordRaceEntry? =
      entrants.firstOrNull { it.entryId.player.playerId == user.idLong }
}

enum class DiscordRaceType {
  GENERIC, LOCKOUT,
}

enum class DiscordRaceState {
  OPEN, STARTING, PROGRESS, FINISHED, ABORTED
}
