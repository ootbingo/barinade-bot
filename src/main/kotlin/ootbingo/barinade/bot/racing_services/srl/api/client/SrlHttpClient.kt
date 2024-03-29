package ootbingo.barinade.bot.racing_services.srl.api.client

import ootbingo.barinade.bot.racing_services.srl.api.SrlApiProperties
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlGame
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPastRaces
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPlayer
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.stream.Collectors
import java.util.stream.IntStream

@Component
class SrlHttpClient(private val properties: SrlApiProperties, private val srlRestTemplate: RestTemplate) {

  fun getGameByAbbreviation(gameAbbreviation: String): SrlGame? {
    return try {
      srlRestTemplate.getForObject(URI.create("${properties.baseUrl}/games/$gameAbbreviation"), SrlGame::class.java)
    } catch (e: HttpClientErrorException) {
      null
    }
  }

  fun getRacesByPlayerName(playerName: String): List<SrlPastRace> {
    val pastRaces =
        srlRestTemplate.getForObject(URI.create("${properties.baseUrl}/pastraces?player=$playerName&pageSize=2000"),
            SrlPastRaces::class.java)

    return pastRaces?.pastraces ?: emptyList()
  }

  fun getPlayerByName(name: String): SrlPlayer? {

    val srlPlayer =
        try {
          srlRestTemplate.getForObject(URI.create("${properties.baseUrl}/players/$name"), SrlPlayer::class.java)
        } catch (e: HttpClientErrorException) {
          return null
        }

    return if (srlPlayer != null && srlPlayer.id == 0L) null else srlPlayer
  }

  fun getAllRacesOfGame(gameAbbreviation: String): Set<SrlPastRace> {

    fun getPage(page: Int) =
        srlRestTemplate
            .getForObject(URI.create("${properties.baseUrl}/pastraces?game=$gameAbbreviation&pageSize=2000&page=$page"),
                SrlPastRaces::class.java)

    return IntStream.iterate(1) { it + 1 }
        .mapToObj { getPage(it) }
        .takeWhile { it != null && it.pastraces.isNotEmpty() }
        .map { it!!.pastraces }
        .flatMap { it.stream() }
        .collect(Collectors.toSet())
  }
}
