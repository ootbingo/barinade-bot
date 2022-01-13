package ootbingo.barinade.bot.racing_services.bingosync

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class BingosyncHttpClientTest {

  //<editor-fold desc="Setup">

  private val restTemplateMock = mock<RestTemplate>()
  private val properties = BingosyncProperties("")

  private val client = BingosyncHttpClient(restTemplateMock, properties)

  private var returnedHtml: AtomicReference<String?>? = null
  private var returnedLocationHeader: AtomicReference<String?>? = null

  @BeforeEach
  internal fun setup() {
    whenever(restTemplateMock.getForEntity(any<String>(), eq(String::class.java)))
        .thenReturn(ResponseEntity.ok(UUID.randomUUID().toString()))

    whenever(restTemplateMock.postForEntity(any<String>(), any<String>(), eq(String::class.java)))
        .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
  }

  //</editor-fold>

  //<editor-fold desc="Test: GET Homepage">

  @Test
  internal fun requestsHomepage() {

    val baseUrl = UUID.randomUUID().toString()

    givenBaseUrl(baseUrl)

    whenHomepageIsRequested()

    thenGetIsSentTo(baseUrl)
  }

  @Test
  internal fun returnsHtml() {

    val html = UUID.randomUUID().toString()

    givenHomepageHtml(html)

    whenHomepageIsRequested()

    thenClientReturnsHtml(html)
  }

  @Test
  internal fun returnsNullIfHomepageReturnsError() {

    givenErrorOnGet()

    whenHomepageIsRequested()

    thenClientReturnsHtml(null)
  }

  //</editor-fold>

  //<editor-fold desc="Test: POST Form">

  @Test
  internal fun sendsCorrectPostRequest() {

    val baseUrl = UUID.randomUUID().toString()
    val configString = UUID.randomUUID().toString()
    val csrfToken = UUID.randomUUID().toString()

    val configMock = mock<BingosyncRoomConfig>()
    whenever(configMock.toHttpPayload()).thenReturn(configString)

    givenBaseUrl(baseUrl)

    whenFormPostIsRequested(configMock, csrfToken)

    thenPostTo(baseUrl).hasBody("$configString&csrfmiddlewaretoken=$csrfToken")
  }

  @Test
  internal fun returnsLocationHeader() {

    val baseUrl = UUID.randomUUID().toString()
    val header = UUID.randomUUID().toString()

    givenBaseUrl(baseUrl)
    givenLocationHeader(header)

    whenFormPostIsRequested(mock(), "")

    thenClientReturnsLocationHeader(baseUrl + header)
  }

  @Test
  internal fun returnsNullIfFormReturnsError() {

    givenErrorOnPost()

    whenFormPostIsRequested(mock(), "")

    thenClientReturnsLocationHeader(null)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenBaseUrl(url: String) {
    properties.baseUrl = url
  }

  private fun givenHomepageHtml(html: String) {
    whenever(restTemplateMock.getForEntity(any<String>(), eq(String::class.java)))
        .thenReturn(ResponseEntity.ok(html))
  }

  private fun givenErrorOnGet() {
    whenever(restTemplateMock.getForEntity(any<String>(), eq(String::class.java)))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build())
  }

  private fun givenLocationHeader(header: String) {
    whenever(restTemplateMock.postForEntity(any<String>(), any<HttpEntity<String>>(), eq(String::class.java)))
        .thenReturn(ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, header).build())
  }

  private fun givenErrorOnPost() {
    whenever(restTemplateMock.postForEntity(any<String>(), any<String>(), eq(String::class.java)))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build())
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenHomepageIsRequested() {
    returnedHtml = AtomicReference(client.getHomepageHtml())
  }

  private fun whenFormPostIsRequested(config: BingosyncRoomConfig, csrfToken: String) {
    returnedLocationHeader = AtomicReference(client.openRoom(config, csrfToken))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenGetIsSentTo(expectedUrl: String) {
    verify(restTemplateMock).getForEntity(expectedUrl, String::class.java)
  }

  private fun thenClientReturnsHtml(expectedHtml: String?) {
    assertThat(returnedHtml).hasValue(expectedHtml)
  }

  private fun thenPostTo(expectedUrl: String) = expectedUrl

  private infix fun String.hasBody(expectedBody: String) {

    val captor = argumentCaptor<HttpEntity<String>>()

    verify(restTemplateMock).postForEntity(eq(this), captor.capture(), eq(String::class.java))

    assertThat(captor.lastValue.body).isEqualTo(expectedBody)
  }

  private fun thenClientReturnsLocationHeader(expectedHeader: String?) {
    assertThat(returnedLocationHeader).hasValue(expectedHeader)
  }

  //</editor-fold>
}

