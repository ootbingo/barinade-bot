package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import java.util.*

class RacetimeAction(
    val action: String,
    val data: RacetimeActionPayload,
)

sealed class RacetimeActionPayload {

  abstract fun asAction(): RacetimeAction
}

class SendMessage(
    val message: String,
    val pinned: Boolean = false,
    val actions: Map<String, RacetimeActionButton>? = null,
    val guid: String = "${UUID.randomUUID()}",
) : RacetimeActionPayload() {

  override fun asAction() = RacetimeAction("message", this)
}

class SetGoal(val info: String) : RacetimeActionPayload() {

  override fun asAction() = RacetimeAction("setinfo", this)
}

class RacetimeActionButton(
    val message: String? = null,
    val url: String? = null,
    val helpText: String? = null,
    val survey: List<RacetimeSurvey>? = null,
    val submit: String? = null,
)

class RacetimeSurvey(
    val name: String,
    val label: String,
    val type: RacetimeSurveyType,
    val default: String? = null,
    val helpText: String? = null,
    val placeholer: String? = null,
    val options: Map<String, String>? = null,
)

enum class RacetimeSurveyType {
  input, bool, radio, select
}
