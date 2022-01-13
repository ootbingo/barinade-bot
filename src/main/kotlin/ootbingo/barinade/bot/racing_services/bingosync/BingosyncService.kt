package ootbingo.barinade.bot.racing_services.bingosync

import org.springframework.stereotype.Service

@Service
class BingosyncService(private val client: BingosyncHttpClient) {

  fun openBingosyncRoom(config: BingosyncRoomConfig): String? =
      client.getHomepageHtml()
          ?.also {
            it.lines()
                .filter { l -> l.contains("<input") }
                .forEach { l -> println(l) }
          }
          ?.readCsrfToken()
          ?.also {
            println()
            println(it)
            println()
          }
          ?.let { client.openRoom(config, it) }

  private fun String.readCsrfToken() =
      lines()
          .firstOrNull { it.contains("csrfmiddlewaretoken") }
          ?.substringAfter("value='")
          ?.substringBefore("'")
}
