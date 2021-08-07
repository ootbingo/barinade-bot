package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import java.util.*

class RacetimeAction(
    val action: String,
    val data: RacetimeActionPayload,
)

sealed class RacetimeActionPayload {

  abstract fun asAction(): RacetimeAction
}

class SendMessage(val message: String, val guid: String = "${UUID.randomUUID()}") : RacetimeActionPayload() {

  override fun asAction() = RacetimeAction("message", this)
}

class SetGoal(val info: String) : RacetimeActionPayload() {

  override fun asAction() = RacetimeAction("setinfo", this)
}
