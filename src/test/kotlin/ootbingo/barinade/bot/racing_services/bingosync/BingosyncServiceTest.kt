package ootbingo.barinade.bot.racing_services.bingosync

import org.mockito.kotlin.mock

internal class BingosyncServiceTest {

  private val bingosyncHttpClientMock = mock<BingosyncHttpClient>()

  private val service = BingosyncService(bingosyncHttpClientMock)
}
