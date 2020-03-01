package ootbingo.barinade.bot.srl.sync

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.stream.Collectors
import java.util.stream.IntStream

internal class SrlSyncJobTest {

  private val srlHttpClientMock = mock<SrlHttpClient>()
  private val srlPlayerImporterMock = mock<SrlPlayerImporter>()
  private val job = SrlSyncJob(srlHttpClientMock, srlPlayerImporterMock)

  @Test
  internal fun importsAllOotPlayers() {

    val players = IntStream
        .range(1, 25)
        .mapToObj { UUID.randomUUID().toString() }
        .collect(Collectors.toSet())

    whenever(srlHttpClientMock.getPlayerNamesOfGame("oot")).thenReturn(players)

    job.execute()

    players.forEach { verify(srlPlayerImporterMock).importPlayer(it) }
    verifyNoMoreInteractions(srlPlayerImporterMock)
  }
}
