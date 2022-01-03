package ootbingo.barinade.bot.discord

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.springframework.stereotype.Component

@Component
class DiscordChannelService {

  fun createChannel(block: ChannelCreationBuilder.() -> Unit): TextChannel? =
      ChannelCreationBuilder().apply(block)
          .takeIf { it.guild != null && it.name.isNotBlank() }
          ?.run {
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
