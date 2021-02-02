package ootbingo.barinade.bot.racing_services.racetime.racing

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.UUID

internal class RaceMonitorTest {

  private val websocketBase = "wss://test.org"

  private val httpClientMock = mock<RacetimeHttpClient>()
  private val raceConnectionFactoryMock = mock<RaceConnectionFactory>()
  private val racetimeApiProperties = RacetimeApiProperties().apply { websocketBaseUrl = websocketBase }
  private val monitor = RaceMonitor(httpClientMock, raceConnectionFactoryMock, racetimeApiProperties)

  private val openRaces = mutableListOf<RacetimeRace>()

  @BeforeEach
  internal fun setup() {
    whenever(httpClientMock.getOpenRaces()).thenReturn(openRaces)
  }

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"])
  internal fun opensRoomForOpenRaces(status: RacetimeRace.RacetimeRaceStatus) {

    val slug1 = UUID.randomUUID().toString()
    val slug2 = UUID.randomUUID().toString()
    val slug3 = UUID.randomUUID().toString()

    givenOpenRace(slug1, status)
    givenOpenRace(slug2, status)
    givenOpenRace(slug3, status)

    whenScanningForRaces()

    thenConnectionsAreOpenedToRooms(slug1, slug2, slug3)
  }

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class, names = ["PENDING", "IN_PROGRESS", "FINISHED", "CANCELLED"])
  internal fun doesNotOpenRoomForOngoingRaces(status: RacetimeRace.RacetimeRaceStatus) {

    val slug1 = UUID.randomUUID().toString()
    val slug2 = UUID.randomUUID().toString()
    val slug3 = UUID.randomUUID().toString()

    givenOpenRace(slug1, status)
    givenOpenRace(slug2, status)
    givenOpenRace(slug3, status)

    whenScanningForRaces()

    thenConnectionsAreOpenedToRooms()
  }

  private fun givenOpenRace(slug: String, status: RacetimeRace.RacetimeRaceStatus) {
    openRaces.add(RacetimeRace("oot/$slug", status))
  }

  private fun whenScanningForRaces() {
    monitor.scanForRaces()
  }

  private fun thenConnectionsAreOpenedToRooms(vararg slugs: String) {
    if (slugs.isEmpty()) {
      verifyZeroInteractions(raceConnectionFactoryMock)
    } else {
      slugs.forEach {
        verify(raceConnectionFactoryMock, times(1)).openConnection("$websocketBase/ws/o/bot/$it")
      }
    }
  }
}