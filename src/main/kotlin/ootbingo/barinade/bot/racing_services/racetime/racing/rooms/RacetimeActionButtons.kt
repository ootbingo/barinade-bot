package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.configuration.RacetimeSurveyTypeSerializer

@Serializable
class RacetimeActionButton(
    val message: String? = null,
    val url: String? = null,
    val helpText: String? = null,
    val survey: List<RacetimeSurvey>? = null,
    val submit: String? = null,
)

@Serializable
class RacetimeSurvey(
    val name: String,
    val label: String,
    val type: RacetimeSurveyType,
    val default: String? = null,
    val helpText: String? = null,
    val placeholer: String? = null,
    val options: Map<String, String>? = null,
)

@Serializable(RacetimeSurveyTypeSerializer::class)
enum class RacetimeSurveyType {

  INPUT, BOOL, RADIO, SELECT
}
