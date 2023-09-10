package ootbingo.barinade.bot.racing_services.bingosync

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("bingosync")
data class BingosyncProperties(
    var baseUrl: String = "",
)
