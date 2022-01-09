package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel

@FunctionalInterface
fun interface DiscordRaceRoomFactory<T : DiscordRaceRoom> {

  fun createRaceRoom(discordChannel: TextChannel): T
}
