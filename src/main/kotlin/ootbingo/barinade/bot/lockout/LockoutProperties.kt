package ootbingo.barinade.bot.lockout

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("ootbingo.lockout")
data class LockoutProperties(
    var discordCategory: String = "disabled",
    var discordChannel: String = "disabled",
)
