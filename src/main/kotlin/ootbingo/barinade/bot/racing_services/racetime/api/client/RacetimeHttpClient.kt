package ootbingo.barinade.bot.racing_services.racetime.api.client

import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeCategory
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRacePage
import ootbingo.barinade.bot.racing_services.racetime.racing.oauth.OAuthManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
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
}
