package ootbingo.barinade.bot.properties

import ootbingo.barinade.bot.properties.model.WhitelistBingo
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("ootbingo.races")
object BingoRaceProperties {

  var blacklist: List<String> = emptyList()
  var whitelist: List<WhitelistBingo> = emptyList()
}
