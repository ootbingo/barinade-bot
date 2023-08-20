package ootbingo.barinade.bot.statistics.validation

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class DateRaceGoalValidator {

  fun validateSrlBingoDate(date: Instant) =
      date.isBefore(ZonedDateTime.of(2019, 9, 21, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant())
}
