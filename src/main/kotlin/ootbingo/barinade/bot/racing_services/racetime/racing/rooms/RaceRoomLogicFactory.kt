package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class RaceRoomLogicFactory(
    private val racetimeHttpClient: RacetimeHttpClient,
) {

  fun <T : RaceRoomLogic> createLogic(type: KClass<T>, delegate: RaceRoomDelegate): T =
      when (type) {
        BingoRaceRoomLogic::class -> BingoRaceRoomLogic(RaceStatusHolder(), delegate)
        AntiBingoRaceRoomLogic::class -> AntiBingoRaceRoomLogic(RaceStatusHolder(), racetimeHttpClient, delegate)
        else -> throw NotImplementedError("Factory for ${type.simpleName} missing")
      } as T

  final inline fun <reified T : RaceRoomLogic> createLogic(delegate: RaceRoomDelegate): T =
      createLogic(T::class, delegate)
}
