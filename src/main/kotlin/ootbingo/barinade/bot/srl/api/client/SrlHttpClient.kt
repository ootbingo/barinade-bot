package ootbingo.barinade.bot.srl.api.client

import ootbingo.barinade.bot.srl.api.SrlApiProperties
import ootbingo.barinade.bot.srl.api.model.SrlGame
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPastRaces
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class SrlHttpClient(private val properties: SrlApiProperties, private val restTemplate: RestTemplate) {

  fun getGameByAbbreviation(gameAbbreviation: String): SrlGame? {
    return try {
      restTemplate.getForObject(URI.create("${properties.baseUrl}/games/$gameAbbreviation"), SrlGame::class.java)
    } catch (e: HttpClientErrorException) {
      null
    }
  }

  fun getRacesByPlayerName(playerName: String): List<SrlPastRace> {
    val pastRaces =
        restTemplate.getForObject(URI.create("${properties.baseUrl}/pastraces?player=$playerName&pageSize=2000"),
                                  SrlPastRaces::class.java)

    return pastRaces?.pastraces ?: emptyList()
  }

  fun getPlayerByName(name: String): SrlPlayer? {
    return restTemplate.getForObject(URI.create("${properties.baseUrl}/players/$name"), SrlPlayer::class.java)
  }
}
