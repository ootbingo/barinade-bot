package ootbingo.barinade.bot.extensions

import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.discord.connection.DiscordMessageInfo
import de.scaramanga.lily.irc.connection.IrcMessageInfo

fun MessageInfo.getUsername(): String? =
    when (this) {
      is DiscordMessageInfo -> this.message.author.name
      is IrcMessageInfo -> this.nick
      else -> null
    }
