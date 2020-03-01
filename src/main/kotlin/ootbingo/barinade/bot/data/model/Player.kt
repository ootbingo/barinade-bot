package ootbingo.barinade.bot.data.model

import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import org.springframework.transaction.annotation.Transactional
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
@Open
data class Player(@Id var srlId: Long = 0,
                  var srlName: String = "",
                  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "player") var raceResults: MutableList<RaceResult> = mutableListOf()) {

  val races: List<Race>
    get() = raceResults.map { it.race }

  constructor(srlPlayer: SrlPlayer, races: List<Race>) :
      this(srlPlayer.id, srlPlayer.name, races.mapNotNull {
        it.raceResults
            .findLast { result -> result.player.srlName == srlPlayer.name }
      }.toMutableList())

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other is Player -> srlId == other.srlId
      else -> false
    }
  }

  override fun hashCode() = srlId.hashCode()

  override fun toString(): String = "Player(srlName=$srlName)"
}
