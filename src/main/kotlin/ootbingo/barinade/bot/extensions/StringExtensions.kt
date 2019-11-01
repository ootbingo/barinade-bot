package ootbingo.barinade.bot.extensions

fun String.containsAny(patterns: Collection<String>): Boolean =
  patterns.any { it in this }
