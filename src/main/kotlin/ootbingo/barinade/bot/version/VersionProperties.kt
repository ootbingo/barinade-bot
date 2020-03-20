package ootbingo.barinade.bot.version

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "barinade")
@PropertySource("classpath:version.properties")
class VersionProperties(
    var version: String? = null,
    var build: String? = null
)
