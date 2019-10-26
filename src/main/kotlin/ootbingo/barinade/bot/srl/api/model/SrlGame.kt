package ootbingo.barinade.bot.srl.api.model

data class SrlGame(var id: Long = 0, var name: String = "", var abbrev: String = "", var popularity: Double = 0.0,
                   var popularityrank: Long = 0) {

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
