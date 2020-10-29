package ootbingo.barinade.bot.racing_services.srl.api.client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class SrlHttpClientConfiguration {

  @Bean
  fun srlRestTemplate(): RestTemplate {
    return RestTemplate()
  }
}
