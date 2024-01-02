package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

interface RaceRoomDelegate {

  fun setGoal(goal: String)
  fun sendMessage(message: String, pinned: Boolean = false, actions: Map<String, RacetimeActionButton>? = null)
  fun closeConnection(delay: Boolean = false)
}
