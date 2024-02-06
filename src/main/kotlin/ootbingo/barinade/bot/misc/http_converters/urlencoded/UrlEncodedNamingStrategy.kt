package ootbingo.barinade.bot.misc.http_converters.urlencoded

fun interface UrlEncodedNamingStrategy {

  fun serializedName(pascalCase: String): String
}

val CamelCaseStrategy = UrlEncodedNamingStrategy {
  it.replaceFirstChar { char -> char.lowercase() }
}

val SnakeCaseStrategy = UrlEncodedNamingStrategy {
  it.replace(Regex("(?<=.)[A-Z]"), "_$0").lowercase()
}
