package ootbingo.barinade.bot.srl.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("com.speedrunslive.api")
data class SrlApiProperties(var baseUrl: String = "")
