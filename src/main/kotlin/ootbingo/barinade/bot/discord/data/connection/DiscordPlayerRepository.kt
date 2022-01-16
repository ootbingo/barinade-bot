package ootbingo.barinade.bot.discord.data.connection

import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.model.DiscordPlayer
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Component

@Component
interface DiscordPlayerRepository : Repository<DiscordPlayer, Long> {

  fun save(player: DiscordPlayer): DiscordPlayer

  fun findById(id: Long): DiscordPlayer?

  fun fromDiscordUser(user: User): DiscordPlayer {

    val player = findById(user.idLong) ?: return save(DiscordPlayer(user.idLong, user.asTag))

    return if (player.name == user.asTag) {
      player
    } else {
      player.name = user.asTag
      save(player)
    }
  }
}
