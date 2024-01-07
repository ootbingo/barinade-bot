package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class RaceRoomLogicFactory {

  fun <T : RaceRoomLogic> createLogic(type: KClass<T>, delegate: RaceRoomDelegate): T =
      when (type) {
        BingoRaceRoomLogic::class -> BingoRaceRoomLogic(RaceStatusHolder(), delegate) as T
        else -> throw NotImplementedError("Factory for ${type.simpleName} missing")
      }

  final inline fun <reified T : RaceRoomLogic> createLogic(delegate: RaceRoomDelegate): T =
      createLogic(T::class, delegate)
}
