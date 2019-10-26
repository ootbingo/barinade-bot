package ootbingo.barinade.bot.srl.api.model

data class SrlGame(val id: Long, val name: String, val abbrev: String, val popularity: Double,
                   val popularityrank: Long) {

  override fun equals(other: Any?): Boolean {

    return when {
      this === other -> true
      other is SrlGame -> id == other.id
      else -> false
    }
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
