package ootbingo.barinade.bot.racing_services.bingosync

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class BingosyncServiceTest {

  //<editor-fold desc="Setup">

  private val bingosyncHttpClientMock = mock<BingosyncHttpClient>()

  private val service = BingosyncService(bingosyncHttpClientMock)

  private var returnedUrl: AtomicReference<String?>? = null

  @BeforeEach
  internal fun setup() {
    givenHomepage("<input type='hidden' name='csrfmiddlewaretoken' value=''>")
  }

  //</editor-fold>

  @Test
  internal fun callsFormWithCorrectCsrfToken() {

    val token = UUID.randomUUID().toString()

    givenHomepage("""
      abc
      <input type='hidden' name='csrfmiddlewaretoken' value='$token'>
      def
    """.trimIndent())

    whenNewRoomIsRequested(randomConfig())

    thenForm isCalledWithCsrfToken token
  }

  @Test
  internal fun callsFormWithCorrectConfig() {

    val config = randomConfig()

    whenNewRoomIsRequested(config)

    thenForm isCalledWithConfig config
  }

  @Test
  internal fun returnsNullIfHomepageCantBeDownloaded() {

    givenHomepage(null)

    whenNewRoomIsRequested(randomConfig())

    thenServiceReturnsRoomUrl(null)
  }

  @Test
  internal fun returnsNullIfCsrfTokenCantBeFound() {

    givenHomepage("This is not a homepage")

    whenNewRoomIsRequested(randomConfig())

    thenServiceReturnsRoomUrl(null)
  }

  @Test
  internal fun returnsNullIfRoomCantBeOpened() {

    givenRoomUrl(null)

    whenNewRoomIsRequested(randomConfig())

    thenServiceReturnsRoomUrl(null)
  }

  @Test
  internal fun returnsRoomUrl() {

    val roomUrl = UUID.randomUUID().toString()

    givenRoomUrl(roomUrl)

    whenNewRoomIsRequested(randomConfig())

    thenServiceReturnsRoomUrl(roomUrl)
  }

  //<editor-fold desc="Given">

  private fun givenHomepage(html: String?) {
    whenever(bingosyncHttpClientMock.getHomepageHtml()).thenReturn(html)
  }

  private fun givenRoomUrl(url: String?) {
    whenever(bingosyncHttpClientMock.openRoom(any(), any())).thenReturn(url)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenNewRoomIsRequested(config: BingosyncRoomConfig) {
    returnedUrl = AtomicReference(service.openBingosyncRoom(config))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private val thenForm: Pair<BingosyncRoomConfig, String>
    get() {
      val configCaptor = argumentCaptor<BingosyncRoomConfig>()
      val csrfCaptor = argumentCaptor<String>()
      verify(bingosyncHttpClientMock).openRoom(configCaptor.capture(), csrfCaptor.capture())
      return configCaptor.lastValue to csrfCaptor.lastValue
    }

  private infix fun Pair<BingosyncRoomConfig, String>.isCalledWithConfig(expectedConfig: BingosyncRoomConfig) {
    assertThat(first).isEqualTo(expectedConfig)
  }

  private infix fun Pair<BingosyncRoomConfig, String>.isCalledWithCsrfToken(expectedToken: String) {
    assertThat(second).isEqualTo(expectedToken)
  }

  private fun thenServiceReturnsRoomUrl(expectedUrl: String?) {
    assertThat(returnedUrl).hasValue(expectedUrl)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun randomConfig() = bingosyncRoomConfig {
    name = UUID.randomUUID().toString()
    password = UUID.randomUUID().toString()
    variant = BingosyncRoomConfig.Variant.BLACKOUT
    lockout = true
    seed = 1337
    hideCard = false
  }

//</editor-fold>
}
