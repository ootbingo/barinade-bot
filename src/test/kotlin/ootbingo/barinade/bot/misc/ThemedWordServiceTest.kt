package ootbingo.barinade.bot.misc

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class ThemedWordServiceTest {

  //<editor-fold desc="Setup">

  private val supplierMock = mock<() -> List<String>>()

  private val service = ThemedWordService(supplierMock)

  private var returnedWord: AtomicReference<String?>? = null

  //</editor-fold>

  @Test
  internal fun returnsOnlyWordFromList() {

    val word = UUID.randomUUID().toString()

    givenWordList(listOf(word))

    whenThemedWordIsRequested(word.length)

    thenServiceReturnsWord(word)
  }

  @Test
  internal fun returnsNullIfWordTooLong() {

    givenWordList(listOf("abc"))

    whenThemedWordIsRequested(2)

    thenServiceReturnsWord(null)
  }

  @Test
  internal fun returnsRandomEntry() {

    val size = 100
    val list = (1..size).map { UUID.randomUUID().toString() }

    givenWordList(list)

    val returnValues = (1..size).map { service.randomWord() }

    returnValues.forEach { assertThat(list).contains(it) }
    assertThat(returnValues.toSet()).hasSizeGreaterThan(1)
  }

  //<editor-fold desc="Given">

  private fun givenWordList(list: List<String>) {
    whenever(supplierMock.invoke()).thenReturn(list)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenThemedWordIsRequested(maxLength: Int = Int.MAX_VALUE) {
    returnedWord = AtomicReference(service.randomWord(maxLength))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenServiceReturnsWord(expectedWord: String?) {
    assertThat(returnedWord).hasValue(expectedWord)
  }

  //</editor-fold>
}
