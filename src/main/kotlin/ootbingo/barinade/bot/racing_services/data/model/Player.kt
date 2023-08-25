package ootbingo.barinade.bot.racing_services.data.model

import jakarta.persistence.*

@Entity
data class Player(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var srlId: Long? = 0,
    var racetimeId: String? = null,
    var srlName: String? = "",
    var racetimeName: String? = null,
    @OneToMany(mappedBy = "resultId.player")
    var raceResults: MutableList<RaceResult> = mutableListOf(),
) {

  val races: List<Race>
    get() = raceResults.map { it.resultId.race }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other is Player -> id == other.id
      else -> false
    }
  }

  override fun hashCode() = id.hashCode()

  override fun toString(): String = "Player(srlName=$srlName, racetimeName=$racetimeName)"
}
