package ootbingo.barinade.bot.extensions

import kotlin.math.floor
import kotlin.streams.toList

fun List<Long>.median(): Long {

  val sorted = this.stream()
      .sorted()
      .toList()

  val n = sorted.size

  return if (n % 2 != 0)
    sorted[n / 2]
  else
    floor(0.5 * sorted[n / 2 - 1] + 0.5 * sorted[n / 2]).toLong()
}
