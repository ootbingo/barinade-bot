package ootbingo.barinade.bot.misc

import kotlin.random.Random

fun generateFilename(): String {

  val charPool: List<Char> = ('A'..'Z').toList()

  return (1..2)
      .map { Random.nextInt(0, charPool.size) }
      .map(charPool::get)
      .joinToString("")
}
