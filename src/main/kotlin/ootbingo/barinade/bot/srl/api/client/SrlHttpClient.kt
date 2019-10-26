package ootbingo.barinade.bot.srl.api.client

import ootbingo.barinade.bot.srl.api.SrlApiProperties
import ootbingo.barinade.bot.srl.api.model.SrlGame
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class SrlHttpClient(private val properties:SrlApiProperties, private val restTemplate: RestTemplate) {

  fun getGameByAbbreviation(gameAbbreviation: String): SrlGame? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun getRacesByPlayerName(playerName: String): List<SrlPastRace> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun getPlayerByName(name: String): SrlPlayer? {
    return restTemplate.getForObject(URI.create("${properties.baseUrl}/players/$name"), SrlPlayer::class.java)
  }
}