package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.User

data class DiscordEntrant(
    private val id: Long,
    private val username: String,
) {

  constructor(discordUser: User) : this(discordUser.idLong, discordUser.asTag)
}
