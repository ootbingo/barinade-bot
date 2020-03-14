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
  private val job = SrlSyncJob(srlHttpClientMock)

  @Test
  internal fun importsAllOotPlayers() {
    TODO()
  }
}
