package ootbingo.barinade.bot.racing_services.bingosync

data class BingosyncRoomConfig(
    val name: String,
    val password: String,
    val variant: Variant,
    val lockout: Boolean = false,
    val seed: Int? = null,
    val hideCard: Boolean = true,
) {

  fun toHttpPayload() =
      buildMap<String, Any> {
        put("room_name", name)
        put("passphrase", password)
        put("nickname", "BingoBot")
        put("game_type", 1)
        put("variant_type", variant.id)
        put("lockout_mode", if (lockout) 2 else 1)
        put("seed", (seed ?: ""))
        put("is_spectator", "on")
        if (hideCard) put("hide_card", "on")
      }.asQueryString()

  private fun Map<String, Any>.asQueryString() =
      map { "${it.key}=${it.value}" }.joinToString("&")

  @Suppress("unused")
  enum class Variant(val id: Int) {

    NORMAL(1), BLACKOUT(20), SHORT(21), SHORT_BLACKOUT(22)
  }
}
