package ootbingo.barinade.bot.racing_services.bingosync

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bingosync")
class BingosyncProperties(
    var baseUrl: String,
)
