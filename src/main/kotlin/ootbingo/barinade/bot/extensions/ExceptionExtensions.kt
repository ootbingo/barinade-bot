package ootbingo.barinade.bot.extensions

val Throwable.description: String
  get() = "${javaClass.simpleName}: $message"

fun <T> T.throwIf(e: Exception, block: (T) -> Boolean): T =
    this.takeUnless(block) ?: throw e
