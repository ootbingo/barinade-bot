package ootbingo.barinade.bot.racing_services.racetime.sync

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.junit.jupiter.api.Test

internal class RacetimeSyncJobTest {

  private val httpClientMock = mock<RacetimeHttpClient>()
  private val importerMock = mock<RacetimeImporter>()

  private val job = RacetimeSyncJob({ importerMock }, httpClientMock)

  @Test
  internal fun passesRacesFromClientToImporter() {

    val allRaces = setOf(RacetimeRace(), RacetimeRace(), RacetimeRace())
    whenever(httpClientMock.getAllRaces()).thenReturn(allRaces)

    job.execute()

    verify(importerMock, times(1)).import(allRaces)
    verifyNoMoreInteractions(importerMock)
  }
}
