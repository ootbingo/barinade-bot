package ootbingo.barinade.bot.racing_services.bingosync

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class BingosyncHttpClient(private val restTemplate: RestTemplate, private val properties: BingosyncProperties) {

  fun getHomepageHtml(): String? =
      restTemplate.getForEntity(properties.baseUrl, String::class.java)
          .takeIf { it.statusCode == HttpStatus.OK }
          ?.body

  fun openRoom(config: BingosyncRoomConfig, csrfToken: String): String? =
      "${config.toHttpPayload()}&csrfmiddlewaretoken=$csrfToken"
          .let { restTemplate.postForEntity(properties.baseUrl, it, String::class.java) }
          .takeIf { it.statusCode == HttpStatus.FOUND }
          ?.let { properties.baseUrl + it.headers[HttpHeaders.LOCATION]?.firstOrNull() }
}
