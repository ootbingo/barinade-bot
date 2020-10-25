package ootbingo.barinade.bot.racing_services.racetime.api.client

import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRacePage
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.stream.Collectors
import java.util.stream.IntStream

@Component
class RacetimeHttpClient(private val racetimeRestTemplate: RestTemplate,
                         private val properties: RacetimeApiProperties) {

  fun getAllRacesOfGame(gameAbbreviation: String): Set<RacetimeRace> {

    fun getPage(page: Int) =
        "${properties.baseUrl}/$gameAbbreviation/races/data?show_entrants=true&page=$page"
            .let { racetimeRestTemplate.getForObject(URI.create(it), RacetimeRacePage::class.java) }

    return IntStream.iterate(1) { it + 1 }
        .mapToObj { getPage(it) }
        .takeWhile { it != null && it.races.isNotEmpty() }
        .map { it!!.races }
        .flatMap { it.stream() }
        .collect(Collectors.toSet())
  }
}
