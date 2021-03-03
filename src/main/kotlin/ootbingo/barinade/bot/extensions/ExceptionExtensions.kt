package ootbingo.barinade.bot.extensions

val Throwable.description: String
  get() = "${javaClass.simpleName}: $message"
