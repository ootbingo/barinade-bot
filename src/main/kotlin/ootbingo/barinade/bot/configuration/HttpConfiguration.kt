package ootbingo.barinade.bot.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class HttpConfiguration {

  @Bean
  fun getRestTemplate(): RestTemplate {
    return RestTemplate()
  }
}
