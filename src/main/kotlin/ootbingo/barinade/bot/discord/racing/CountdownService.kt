package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.MessageChannel
import org.springframework.stereotype.Component

@Component
class CountdownService(private val wait: WaitWrapper) {

  fun postCountdownInChannel(channel: MessageChannel) {
    channel.sendMessageBlocking("The race is about to start...")
    (10 downTo 1).forEach {
      wait(1000)
      channel.sendMessageBlocking(it.toString())
    }
  }

  private fun MessageChannel.sendMessageBlocking(message: String) =
      sendMessage(message).complete()
}
