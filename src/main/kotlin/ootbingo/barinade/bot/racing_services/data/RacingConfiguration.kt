package ootbingo.barinade.bot.racing_services.data

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.stream.Collectors

@Configuration
class RacingConfiguration {

  @Bean
  fun userMappingCsv(): String {
    return ClassPathResource("user_mapping.csv")
        .inputStream
        .bufferedReader()
        .lines()
        .collect(Collectors.joining("\n"))
  }
}
