package ootbingo.barinade.bot.extensions

import de.scaramangado.lily.core.communication.MessageInfo
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import de.scaramangado.lily.irc.connection.IrcMessageInfo
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo

fun MessageInfo.getUsername(): String? =
    when (this) {
      is DiscordMessageInfo -> this.message.author.name
      is IrcMessageInfo -> this.nick
      is RacetimeMessageInfo -> this.message.user?.name
      else -> null
    }
