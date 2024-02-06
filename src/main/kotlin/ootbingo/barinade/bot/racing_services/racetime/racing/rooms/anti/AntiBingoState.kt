package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser

data class AntiBingoState(
    val entrants: List<RacetimeUser>,
    val entrantMappings: List<EntrantMapping>,
) {

  data class EntrantMapping(
      val entrant: RacetimeUser,
      val choosesFor: RacetimeUser,
      var chosenRow: Row?,
  )

  enum class Row {

    ROW1, ROW2, ROW3, ROW4, ROW5,
    COL1, COL2, COL3, COL4, COL5,
    TLBR, BLTR;

    companion object {

      fun asStringMap(): Map<String, String> = entries.associate { it.name to it.formatted }
    }
  }
}

val AntiBingoState.Row.formatted: String
  get() = if (name.startsWith("ROW") || name.startsWith("COL")) {
    name.replace("ROW", "Row ").replace("COL", "Col ")
  } else {
    buildString {
      append(name)
      insert(2, "-")
    }
  }
