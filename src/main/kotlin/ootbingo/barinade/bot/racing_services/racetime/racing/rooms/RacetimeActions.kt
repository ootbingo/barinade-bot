package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class RacetimeAction(
    val action: String,
    val data: RacetimeActionPayload,
)

@Serializable
sealed class RacetimeActionPayload {

  abstract fun asAction(): RacetimeAction
}

@Serializable
class SendMessage(
    val message: String,
    val pinned: Boolean = false,
    val actions: Map<String, RacetimeActionButton>? = null,
    val guid: String = "${UUID.randomUUID()}",
) : RacetimeActionPayload() {

  override fun asAction() = RacetimeAction("message", this)
}

@Serializable
class SetGoal(val info: String) : RacetimeActionPayload() {

  override fun asAction() = RacetimeAction("setinfo", this)
}
