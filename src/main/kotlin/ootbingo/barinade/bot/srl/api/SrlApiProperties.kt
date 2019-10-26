package ootbingo.barinade.bot.srl.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@ConfigurationProperties("com.speedrunslive.api")
@Component
data class SrlApiProperties(var baseUrl: URI = URI.create("http://api.speedrunslive.com/"))
