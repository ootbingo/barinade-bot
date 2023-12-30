package ootbingo.barinade.bot.racing_services.racetime.api.client

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import java.util.stream.IntStream

const val OOT: String = "oot"

@Component
class RacetimeHttpClient(
    private val racetimeRestTemplate: RestTemplate,
    private val properties: RacetimeApiProperties,
    private val oAuthManager: OAuthManager,
    private val racetimeJson: Json,
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
    val json = racetimeJson.encodeToString(editedRace).replace(Regex("([{}])"), "")
    val formString = json.split(",").map { field ->
      field.split(":").let { trim(it[0]) to trim(it[1]) }
    }.joinToString("&") { "${it.first}=${encode(it.second)}" }

    racetimeRestTemplate.postForEntity<RacetimeRace>(
        "${properties.racingBaseUrl}/o/$OOT/$slug/edit",
        HttpEntity(
            formString,
            HttpHeaders().apply {
              setBearerAuth(oAuthManager.getToken())
              contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
        ),
    )
  }

  private fun trim(string: String): String {

    val withoutWhitespace = string.trim()
    return withoutWhitespace.replace("\"", "")
  }

  private fun encode(string: String): String {
    return URLEncoder.encode(string, StandardCharsets.UTF_8)
  }
}
