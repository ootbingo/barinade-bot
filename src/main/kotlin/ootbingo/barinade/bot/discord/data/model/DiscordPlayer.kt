package ootbingo.barinade.bot.discord.data.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class DiscordPlayer(

    @Id
    var playerId: Long = 0,
    var name: String = "",
)
