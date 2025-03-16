package ootbingo.barinade.bot.racing_services.racetime.api.client

import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import ootbingo.barinade.bot.racing_services.racetime.api.model.*
import ootbingo.barinade.bot.racing_services.racetime.racing.oauth.OAuthManager
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity
import java.util.stream.Collectors
import java.util.stream.IntStream

const val OOT: String = "oot"

@Component
class RacetimeHttpClient(
  private val racetimeRestTemplate: RestTemplate,
  private val properties: RacetimeApiProperties,
  private val oAuthManager: OAuthManager,
) {

  private val logger = LoggerFactory.getLogger(RacetimeHttpClient::class.java)

  fun getAllRaces(): Collection<RacetimeRace> {

    fun getPage(page: Int) =
      "${properties.dataBaseUrl}/$OOT/races/data?show_entrants=true&page=$page"
        .let { racetimeRestTemplate.getForObject<RacetimeRacePage>(it) }

    return IntStream.iterate(1) { it + 1 }
      .mapToObj { getPage(it) }
      .takeWhile { it != null && it.races.isNotEmpty() }
      .map { it!!.races }
      .flatMap { it.stream() }
      .collect(Collectors.toSet())
  }

  fun getOpenRaces(): Collection<RacetimeRace> {
    val oot = racetimeRestTemplate
      .getForEntity<RacetimeCategory>("${properties.racingBaseUrl}/$OOT/data")
      .body

    return oot?.currentRaces ?: emptySet()
  }

  fun startRace(data: RacetimeEditableRace): String {

    val response = racetimeRestTemplate.postForEntity<Unit>(
      "${properties.racingBaseUrl}/o/$OOT/startrace",
      HttpEntity(
        data,
        HttpHeaders().apply {
          setBearerAuth(oAuthManager.getToken())
          contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
      )
    )

    if (response.statusCode != HttpStatus.CREATED) {
      logger.error("Unexpected status ${response.statusCode}")
      throw RuntimeException("Failed to open race room")
    }

    return response.headers.location?.toString()?.let { properties.racingBaseUrl + it }
      ?: throw IllegalStateException("Response did not include location header")
  }

  fun editRace(slug: String, edits: RacetimeEditableRace.() -> Unit) {

    val race = racetimeRestTemplate
      .getForEntity<RacetimeRace>("${properties.racingBaseUrl}/$OOT/$slug/data")
      .body
      ?: throw IllegalStateException("Race $slug not found")

    val editedRace = race.toEditableRace().apply(edits)

    racetimeRestTemplate.postForEntity<RacetimeRace>(
      "${properties.racingBaseUrl}/o/$OOT/$slug/edit",
      HttpEntity(
        editedRace,
        HttpHeaders().apply {
          setBearerAuth(oAuthManager.getToken())
          contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
      ),
    )
  }

  fun getLeaderboards(): List<RacetimeLeaderboard> =
    racetimeRestTemplate.getForEntity<RacetimeLeaderboards>("${properties.dataBaseUrl}/$OOT/leaderboards/data")
      .body
      ?.leaderboards
      ?: emptyList()
}
