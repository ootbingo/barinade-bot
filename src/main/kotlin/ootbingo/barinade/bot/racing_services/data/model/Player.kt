package ootbingo.barinade.bot.racing_services.data.model

import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPlayer
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
@Open
data class Player(@Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
                  var srlId: Long? = 0,
                  var racetimeId: String? = null,
                  var srlName: String? = "",
                  var racetimeName: String? = null,
                  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "resultId.player")
                  var raceResults: MutableList<RaceResult> = mutableListOf()) {

  val races: List<Race>
    get() = raceResults.map { it.resultId.race }

  constructor(srlPlayer: SrlPlayer, races: List<Race>) :
      this(null, srlPlayer.id, null, srlPlayer.name, null, races.mapNotNull {
        it.raceResults
            .findLast { result -> result.resultId.player.srlName == srlPlayer.name }
      }.toMutableList())

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
