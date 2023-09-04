package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

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
  INPUT, BOOL, RADIO, SELECT
}
