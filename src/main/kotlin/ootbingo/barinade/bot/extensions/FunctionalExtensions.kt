package ootbingo.barinade.bot.extensions

inline fun <reified T> Any.castOrNull(): T? = this as? T

fun <T, U> Pair<T?, U>.checkFirstForNull(): Pair<T, U>? =
    first?.let { first!! to second }

fun Boolean.ifFalse(block: () -> Unit): Boolean =
    this.also { this.takeIf { !it }?.run { block() } }
