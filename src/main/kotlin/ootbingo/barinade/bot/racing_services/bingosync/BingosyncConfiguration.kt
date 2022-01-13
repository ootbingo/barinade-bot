package ootbingo.barinade.bot.racing_services.bingosync

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection

@Configuration
class BingosyncConfiguration {

  @Bean
  fun bingosyncRestTemplate(): RestTemplate =
      RestTemplateBuilder()
          .requestFactory {
            object : SimpleClientHttpRequestFactory() {
              override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                super.prepareConnection(connection, httpMethod)
                connection.instanceFollowRedirects = false
              }
            }
          }
          .errorHandler(object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean = false
            override fun handleError(response: ClientHttpResponse) {}
          })
          .build()
}
