package ootbingo.barinade.bot.racing_services.bingosync

import ootbingo.barinade.bot.extensions.exception
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BingosyncHttpClient(private val bingosyncRestTemplate: RestTemplate, private val properties: BingosyncProperties) {

  private val logger = LoggerFactory.getLogger(BingosyncHttpClient::class.java)
  private val cookies = mutableMapOf<String, String>()

  fun getHomepageHtml(): String? =
      try {
        bingosyncRestTemplate.getForEntity(properties.baseUrl, String::class.java)
            .takeIf { it.statusCode == HttpStatus.OK }
            ?.saveCookies()
            ?.body
      } catch (e: Exception) {
        logger.exception("Cannot download Bingosync homepage", e)
        null
      }

  fun openRoom(config: BingosyncRoomConfig, csrfToken: String): String? =
      try {
        "${config.toHttpPayload()}&csrfmiddlewaretoken=$csrfToken"
            .let { bingosyncRestTemplate.postForEntity(properties.baseUrl, it.addHeaders(), String::class.java) }
            .takeIf { it.statusCode == HttpStatus.FOUND }
            ?.let { properties.baseUrl + it.headers[HttpHeaders.LOCATION]?.firstOrNull() }
      } catch (e: Exception) {
        logger.exception("Connot open Bingosync room", e)
        null
      }

  private fun <T> ResponseEntity<T>.saveCookies(): ResponseEntity<T> {

    headers["Set-Cookie"]?.forEach {
      cookies[it.substringBefore("=")] = it.substringAfter("=").substringBefore(";")
    }

    return this
  }

  private fun <T> T.addHeaders(): HttpEntity<T> {

    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
    cookies.forEach { c -> headers.add("Cookie", "${c.key}=${c.value}") }

    return HttpEntity(this, headers)
  }
}
