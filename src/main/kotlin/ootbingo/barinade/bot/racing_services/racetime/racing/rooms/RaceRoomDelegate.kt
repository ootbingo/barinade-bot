package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import kotlin.reflect.KClass

interface RaceRoomDelegate {

  fun setGoal(goal: String)
  fun sendMessage(message: String, pinned: Boolean = false, directedTo: String? = null, actions: Map<String, RacetimeActionButton>? = null)
  fun closeConnection(delay: Boolean = false)
  fun <T : RaceRoomLogic> changeLogic(type: KClass<T>)
}

inline fun <reified T : RaceRoomLogic> RaceRoomDelegate.changeLogic() {
  changeLogic(T::class)
}
