package ootbingo.barinade.bot.misc.http_converters.urlencoded

import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.LocalDate
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

@Suppress("unused", "UNUSED_PARAMETER")
class WriteOnlyUrlEncodedHttpMessageConverterTest {

  private val converter = WriteOnlyUrlEncodedHttpMessageConverter(CamelCaseStrategy)

  //<editor-fold desc="Test: canRead">

  @Test
  fun cannotRead() {

    class TestClass(val x: String)

    val soft = SoftAssertions()

    soft.assertThat(converter.canRead(TestClass::class.java, null)).isFalse()
    allMediaTypes.forEach { soft.assertThat(converter.canRead(TestClass::class.java, it)).isFalse() }

    soft.assertAll()
  }

  //</editor-fold>

  //<editor-fold desc="Test: canWrite">

  @Test
  internal fun canWriteUrlencoded() {

    class TestClass(val x: String)

    assertThat(converter.canWrite(TestClass::class.java, MediaType.APPLICATION_FORM_URLENCODED)).isTrue()
  }

  @Test
  fun cannotWriteOtherTypes() {

    class TestClass(val x: String)

    val soft = SoftAssertions()

    soft.assertThat(converter.canWrite(TestClass::class.java, null)).isFalse()
    allMediaTypes
        .filter { it != MediaType.APPLICATION_FORM_URLENCODED }
        .forEach { soft.assertThat(converter.canWrite(TestClass::class.java, it)).isFalse() }

    soft.assertAll()
  }

  @Test
  internal fun cannotWriteClassWithoutGetter() {

    class TestClass(x: String)

    assertThat(converter.canWrite(TestClass::class.java, MediaType.APPLICATION_FORM_URLENCODED)).isFalse()
  }

  @Test
  internal fun cannotWriteClassWithIterableMember() {

    class TestClass(val x: String, val y: List<Int>)

    assertThat(converter.canWrite(TestClass::class.java, MediaType.APPLICATION_FORM_URLENCODED)).isFalse()
  }

  //</editor-fold>

  //<editor-fold desc="Test: getSupportedMediaTypes">

  @Test
  internal fun returnsCorrectMediaTypes() {
    assertThat(converter.supportedMediaTypes).containsExactly(MediaType.APPLICATION_FORM_URLENCODED)
  }

  //</editor-fold>

  //<editor-fold desc="Test: read">

  @Test
  internal fun throwsOnRead() {

    class TestClass(val x: String)

    assertThatCode { converter.read(TestClass::class.java, mock()) }
        .isExactlyInstanceOf(UnsupportedOperationException::class.java)
  }

  //</editor-fold>

  //<editor-fold desc="Test: write">

  @Test
  internal fun writesInCamelCase() {

    val (outputMessageMock, reader) = mockOutputMessage()

    class TestClass(val parameterName: String, val date: LocalDate, val number: Int?)

    val testObject = TestClass("example!String", LocalDate.of(2024, 1, 1), null)

    converter.write(testObject, MediaType.APPLICATION_FORM_URLENCODED, outputMessageMock)

    assertThat(
        reader.readText().split("&").map {
          it.split("=").let { s -> s[0] to s[1] }
        }
    )
        .containsExactlyInAnyOrder(
            "parameterName" to "example%21String",
            "date" to "2024-01-01",
            "number" to "null",
        )
  }

  @Test
  internal fun writesInSnakeCase() {

    val converter = WriteOnlyUrlEncodedHttpMessageConverter(SnakeCaseStrategy)
    val (outputMessageMock, reader) = mockOutputMessage()

    class TestClass(val parameterName: String, val date: LocalDate, val number: Int?)

    val testObject = TestClass("example!String", LocalDate.of(2024, 1, 1), null)

    converter.write(testObject, MediaType.APPLICATION_FORM_URLENCODED, outputMessageMock)

    assertThat(
        reader.readText().split("&").map {
          it.split("=").let { s -> s[0] to s[1] }
        }
    )
        .containsExactlyInAnyOrder(
            "parameter_name" to "example%21String",
            "date" to "2024-01-01",
            "number" to "null",
        )
  }

  @Test
  internal fun identifiesGetterCorrectly() {

    val (outputMessageMock, reader) = mockOutputMessage()

    @Suppress()
    class TestClass(
        val parameterName: String,
        val date: LocalDate,
        val bool: Boolean,
    ) {

      fun getTest() = "test"
      fun isNotAGetter() = "not a getter"
      fun isGetter() = true
      fun notAGetter() = "test"
      fun getting() = "test"
      fun getVoid() {}
      fun getParameter(param: String) = ""
    }

    val testObject = TestClass("example!String", LocalDate.of(2024, 1, 1), true)

    converter.write(testObject, MediaType.APPLICATION_FORM_URLENCODED, outputMessageMock)

    assertThat(
        reader.readText().split("&").map {
          it.split("=").let { s -> s[0] to s[1] }
        }
    )
        .containsExactlyInAnyOrder(
            "parameterName" to "example%21String",
            "date" to "2024-01-01",
            "bool" to "true",
            "test" to "test",
            "getter" to "true",
        )
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private val allMediaTypes: Collection<MediaType> by lazy {
    MediaType::class.members
        .filter { it.returnType.isSubtypeOf(typeOf<MediaType>()) }
        .filter { it.name.matches("[A-Z_]+".toRegex()) }
        .map { it.call() as MediaType }
  }

  private fun mockOutputMessage() = object {

    val inputStream = PipedInputStream()
    val outputStream = PipedOutputStream(inputStream)

    val outputMessage = object : HttpOutputMessage {
      override fun getHeaders(): HttpHeaders = mock()
      override fun getBody(): OutputStream = outputStream
    }

    operator fun component1() = outputMessage
    operator fun component2() = inputStream.bufferedReader()
  }

  //</editor-fold>
}
