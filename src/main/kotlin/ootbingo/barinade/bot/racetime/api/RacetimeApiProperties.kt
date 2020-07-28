package ootbingo.barinade.bot.racetime.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("gg.racetime.api")
data class RacetimeApiProperties(var baseUrl: String = "")
