package ootbingo.barinade.bot.srl.api.client

import ootbingo.barinade.bot.srl.api.model.SrlGame
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import org.springframework.stereotype.Component

@Component
class SrlHttpClient {

  fun getGameByAbbreviation(gameAbbreviation: String): SrlGame? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun getRacesByPlayerName(playerName: String): List<SrlPastRace> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun getPlayerByName(name: String): SrlPlayer? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}