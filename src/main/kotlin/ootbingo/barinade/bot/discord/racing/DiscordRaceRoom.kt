package ootbingo.barinade.bot.discord.racing

abstract class DiscordRaceRoom {

  fun enter(entrant: DiscordEntrant): String? {
    TODO()
  }

  fun unenter(entrant: DiscordEntrant): String? {
    TODO()
  }

  fun ready(entrant: DiscordEntrant): String? {
    TODO()
  }

  fun unready(entrant: DiscordEntrant): String? {
    TODO()
  }

  abstract fun start(entrant: DiscordEntrant): String?
}
