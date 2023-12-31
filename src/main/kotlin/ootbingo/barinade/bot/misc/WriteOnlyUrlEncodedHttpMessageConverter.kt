package ootbingo.barinade.bot.misc

import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import java.lang.reflect.Method
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WriteOnlyUrlEncodedHttpMessageConverter(
    private val namingStrategy: UrlEncodedNamingStrategy = CamelCaseStrategy,
) : HttpMessageConverter<Any> {

  override fun canRead(clazz: Class<*>, mediaType: MediaType?) = false

  override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean =
      if (MediaType.APPLICATION_FORM_URLENCODED != mediaType) false
      else canWriteClass(clazz)

  override fun getSupportedMediaTypes(): MutableList<MediaType> = mutableListOf(MediaType.APPLICATION_FORM_URLENCODED)

  override fun write(t: Any, contentType: MediaType?, outputMessage: HttpOutputMessage) {
    allGetters(t.javaClass)
        .map { it.name to (it.invoke(t)?.toString() ?: "null") }
        .map { serializedName(it.first) to URLEncoder.encode(it.second, StandardCharsets.UTF_8) }
        .joinToString("&") { "${it.first}=${it.second}" }
        .run { outputMessage.body.bufferedWriter().use { it.write(this) } }
  }

  override fun read(clazz: Class<out Any>, inputMessage: HttpInputMessage): Any {
    throw UnsupportedOperationException("Message converter is write only")
  }

  private fun canWriteClass(clazz: Class<*>): Boolean {
    return allGetters(clazz)
        .takeIf { it.isNotEmpty() }
        ?.map { it.returnType }
        ?.none { it.declaredClasses.contains(Iterable::class.java) }
        ?: false
  }

  private fun allGetters(clazz: Class<*>) = clazz.methods.filter(this::isGetter)

  private fun isGetter(method: Method): Boolean {

    if (!method.name.matches(Regex("^(get|is)[A-Z]+.*"))) {
      return false
    }

    if (method.name.startsWith("is") && method.returnType != Boolean::class.java) {
      return false
    }

    if (method.name == "getClass") {
      return false
    }

    if (method.parameters.isNotEmpty()) {
      return false
    }

    if (method.returnType == Void.TYPE) {
      return false
    }

    return true
  }

  private fun serializedName(getterName: String): String {
    return namingStrategy.serializedName(getterName.replace(Regex("^(get|is)"), ""))
  }
}

fun interface UrlEncodedNamingStrategy {

  fun serializedName(raw: String): String
}

val CamelCaseStrategy = UrlEncodedNamingStrategy {
  it.replaceFirstChar { char -> char.lowercase() }
}

val SnakeCaseStrategy = UrlEncodedNamingStrategy {
  it.replace(Regex("(?<=.)[A-Z]"), "_$0").lowercase()
}
