package ootbingo.barinade.bot.srl.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("com.speedrunslive.api")
@Component
data class SrlApiProperties(var baseUrl: String = "https://api.speedrunslive.com")
