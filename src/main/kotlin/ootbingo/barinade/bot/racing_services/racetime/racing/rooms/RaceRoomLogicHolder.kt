package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import kotlin.reflect.KProperty

class RaceRoomLogicHolder {

  var logic: RaceRoomLogic = NoopRaceRoomLogic

  operator fun getValue(thisRef: Any?, property: KProperty<*>): RaceRoomLogic = logic

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: RaceRoomLogic) {
    logic = value
  }
}
