package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

data class AntiBingoState(
    val entrants: List<String>,
    val entrantMappings: List<EntrantMapping>,
) {

  data class EntrantMapping(
      val entrant: String,
      val choosesFor: String,
      val chosenRow: Row?,
  )

  enum class Row {

    ROW1, ROW2, ROW3, ROW4, ROW5,
    COL1, COL2, COL3, COL4, COL5,
    TLBR, BLTR;
  }
}
