package ootbingo.barinade.bot.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("ootbingo.races")
object BingoRaceProperties {

  var blacklist: List<Int> = emptyList()
}
