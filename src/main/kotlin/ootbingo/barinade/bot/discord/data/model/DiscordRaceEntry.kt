package ootbingo.barinade.bot.discord.data.model

import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState.*
import java.io.Serializable
import java.time.Duration
import javax.persistence.*

@Entity
class DiscordRaceEntry(
    @EmbeddedId
    var entryId: EntryId = EntryId(),
    @Enumerated(EnumType.STRING)
    var state: DiscordRaceEntryState = NOT_READY,
    var time: Duration? = null,
    var place: Int? = null,
) {

  @Embeddable
  class EntryId(
      @ManyToOne(cascade = [], fetch = FetchType.LAZY) @JoinColumn(name = "raceId")
      var race: DiscordRace = DiscordRace(),
      @ManyToOne(cascade = [], fetch = FetchType.LAZY) @JoinColumn(name = "playerId")
      var player: DiscordPlayer = DiscordPlayer(),
  ) : Serializable
}

enum class DiscordRaceEntryState {
  NOT_READY, READY, PLAYING, FINISHED, FORFEIT, DQ, NOT_RANKED
}
