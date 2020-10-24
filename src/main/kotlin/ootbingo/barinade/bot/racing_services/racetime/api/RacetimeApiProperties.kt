package ootbingo.barinade.bot.racing_services.racetime.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("gg.racetime.api")
data class RacetimeApiProperties(var baseUrl: String = "")
