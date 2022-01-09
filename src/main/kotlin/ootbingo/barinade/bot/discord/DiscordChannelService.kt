package ootbingo.barinade.bot.discord

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import ootbingo.barinade.bot.extensions.throwIf
import org.springframework.stereotype.Component

@Component
class DiscordChannelService {

  fun createChannel(block: ChannelCreationBuilder.() -> Unit): TextChannel? =
      ChannelCreationBuilder().apply(block)
          .throwIf(NullPointerException("Guild must not be null")) { it.guild == null }
          .throwIf(IllegalArgumentException("Channel name not set")) { it.name.isBlank() }
          .run {
            guild!!.createTextChannel(name, categoryId?.let { guild!!.getCategoryById(it) })
                .submit()
                .get()
          }

  data class ChannelCreationBuilder(
      var name: String = "",
      var categoryId: String? = null,
      var guild: Guild? = null,
  )
}
