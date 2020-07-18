package ootbingo.barinade.bot.data.model

import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
@Open
data class Player(@Id var idSrl: Long = 0,
                  var nameSrl: String = "",
                  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "resultId.player") var raceResults: MutableList<RaceResult> = mutableListOf()) {

  val races: List<Race>
    get() = raceResults.map { it.resultId.race }

  constructor(srlPlayer: SrlPlayer, races: List<Race>) :
      this(srlPlayer.id, srlPlayer.name, races.mapNotNull {
        it.raceResults
            .findLast { result -> result.resultId.player.nameSrl == srlPlayer.name }
      }.toMutableList())

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other is Player -> idSrl == other.idSrl
      else -> false
    }
  }

  override fun hashCode() = idSrl.hashCode()

  override fun toString(): String = "Player(srlName=$nameSrl)"
}
