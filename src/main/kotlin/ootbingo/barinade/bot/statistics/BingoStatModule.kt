package ootbingo.barinade.bot.statistics

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import ootbingo.barinade.bot.extensions.exception
import ootbingo.barinade.bot.extensions.median
import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Duration
import java.util.*

@LilyModule
class BingoStatModule(private val playerHelper: PlayerHelper) {

  private val logger = LoggerFactory.getLogger(BingoStatModule::class.java)

  @LilyCommand("average")
  fun average(chatCommand: Command): Answer<AnswerInfo>? =
      playerHelper.query {

        command = chatCommand

        defaultAmount = 10

        var forfeits = 0

        raceFilter = {
          it
              .filter { r ->
                if (r.resultType == ResultType.FORFEIT) {
                  forfeits++
                  false
                } else {
                  true
                }
              }
        }

        aggregator = {
          it.map { r -> r.time!!.toSeconds() }
              .average()
              .let { avg ->
                "The average of %user%'s last %count% bingos is: " +
                    "${Duration.ofSeconds(avg.toLong()).standardFormat()} (Forfeits: $forfeits)"
              }
        }
      }

  @LilyCommand("median")
  fun median(chatCommand: Command): Answer<AnswerInfo>? =
      playerHelper.query {

        command = chatCommand

        defaultAmount = 15

        var forfeits = 0

        raceFilter = {
          it
              .filter { r ->
                if (r.resultType == ResultType.FORFEIT) {
                  forfeits++
                  false
                } else {
                  true
                }
              }
        }

        aggregator = {
          it.map { r -> r.time!!.toSeconds() }
              .median()
              .let { avg ->
                "The median of %user%'s last %count% bingos is: " +
                    "${Duration.ofSeconds(avg).standardFormat()} (Forfeits: $forfeits)"
              }
        }
      }

  @LilyCommand("forfeits")
  fun forfeitRatio(chatCommand: Command): Answer<AnswerInfo>? =
      playerHelper.query {

        command = chatCommand

        allowDifferentAmounts = false

        aggregator = {

          val total = it.count().toDouble()
          val forfeits = it.count { r -> r.resultType == ResultType.FORFEIT }.toDouble()

          (100 * forfeits / total)
              .let { fr -> DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH)).format(fr) }
              .let { fr -> "The forfeit ratio of %user% is: $fr%" }
        }
      }

  fun median(username: String): Duration? {

    return rawDataQuery(
        username,
        "!median",
        BingoStatModule::median,
        Regex("""The median of \S*'s last \d+ bingos is: (\d+):(\d\d):(\d\d) \(Forfeits: \d+\)"""),
    ) {
      val (hours, minutes, seconds) = (1..3).map { i -> checkNotNull(it[i]?.value?.toLong()) }
      Duration.ofSeconds(3600 * hours + 60 * minutes + seconds)
    }
  }

  fun forfeitRatio(username: String): Double? {

    return rawDataQuery(
        username,
        "!forfeits",
        BingoStatModule::forfeitRatio,
        Regex("""The forfeit ratio of \S* is: (\d{1,3}\.\d\d)%"""),
    ) {
      checkNotNull(it[1]?.value?.toDouble()) / 100.0
    }
  }

  private fun <T> rawDataQuery(
      username: String,
      message: String,
      command: BingoStatModule.(Command) -> Answer<AnswerInfo>?,
      responseRegex: Regex,
      parser: (MatchGroupCollection) -> T,
  ): T? {
    val chatResponse = command(
        ChatMessage(user = RacetimeUser(name = username), message = message)
            .let { Command.withMessageInfo(it.messagePlain, RacetimeMessageInfo(it)) }
    )?.text

    if (
        chatResponse == null ||
        chatResponse.contains("not found") ||
        chatResponse.contains("has not finished any bingos")
    ) {
      return null
    }

    return try {
      responseRegex.matchEntire(chatResponse)?.groups?.let(parser)
    } catch (e: Exception) {
      logger.exception("Failed to calculate raw !$message result for '$username'.\nChat response: $chatResponse", e)
      null
    }
  }
}
