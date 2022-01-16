package ootbingo.barinade.bot.discord.data.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class DiscordPlayer(

    @Id
    var playerId: Long = 0,
    var name: String = "",
)
