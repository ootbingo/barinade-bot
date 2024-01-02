package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import org.springframework.stereotype.Service

@Service
class RaceRoomLogicFactory {

  final inline fun <reified T : RaceRoomLogic> createLogic(delegate: RaceRoomDelegate): T =
      when (T::class) {
        BingoRaceRoomLogic::class -> BingoRaceRoomLogic(RaceStatusHolder(), delegate) as T
        else -> throw NotImplementedError("Factory for ${T::class.simpleName} missing")
      }
}
