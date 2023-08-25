package ootbingo.barinade.bot.racing_services.data.model

import jakarta.persistence.*
import java.time.Instant

@Entity
data class Race(
    @Id var raceId: String = "",
    var goal: String = "",
    var datetime: Instant = Instant.now(),
    @Enumerated(EnumType.STRING)
    var platform: Platform = Platform.SRL,
    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "resultId.race", fetch = FetchType.EAGER)
    var raceResults: MutableList<RaceResult> = mutableListOf(),
)
