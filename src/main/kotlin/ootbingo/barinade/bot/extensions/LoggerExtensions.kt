package ootbingo.barinade.bot.extensions

import org.slf4j.Logger

fun Logger.exception(message: String, throwable: Throwable) {
  error(message)
  error(throwable.description)
  debug("", throwable)
}
