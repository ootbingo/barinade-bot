package ootbingo.barinade.bot.misc

fun interface CommandExecutor {

  fun execute(command: List<String>): String
}
