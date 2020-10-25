package ootbingo.barinade.bot.racing_services.srl.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("com.speedrunslive.api")
data class SrlApiProperties(var baseUrl: String = "")
