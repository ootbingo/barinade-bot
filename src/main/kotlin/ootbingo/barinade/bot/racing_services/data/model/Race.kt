package ootbingo.barinade.bot.racing_services.data.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Entity
data class Race(
    @Id var raceId: String = "",
    var goal: String = "",
    @Convert(converter = TempInstantConverter::class) var datetime: Instant = Instant.now(),
    @Enumerated(EnumType.STRING)
    var platform: Platform = Platform.SRL,
    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "resultId.race", fetch = FetchType.EAGER)
    var raceResults: MutableList<RaceResult> = mutableListOf(),
)

// TODO Fix DB schema instead
class TempInstantConverter : AttributeConverter<Instant, LocalDateTime> {

  override fun convertToDatabaseColumn(attribute: Instant): LocalDateTime =
      LocalDateTime.ofInstant(attribute, ZoneId.of("UTC"))

  override fun convertToEntityAttribute(dbData: LocalDateTime): Instant =
      dbData.toInstant(ZoneOffset.UTC)
}
