package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.MessageChannel
import org.springframework.stereotype.Component

@Component
class DiscordRaceRoomManager {

  private val roomMap = mutableMapOf<Long, DiscordRaceRoom>()

  fun getRaceRoomForChannel(channel: MessageChannel) = roomMap[channel.idLong]

  fun addRaceRoom(channel: MessageChannel, room: DiscordRaceRoom) {

    if (channel.idLong in roomMap) {
      throw IllegalArgumentException("Race already exists")
    }

    roomMap[channel.idLong] = room
  }
}
