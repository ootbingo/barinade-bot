package ootbingo.barinade.bot.data.model

import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Player(@Id var srlId: Long,
                  var srlName: String,
                  @OneToMany(cascade = [CascadeType.ALL]) var raceResults: MutableList<RaceResult>) {

  val races: List<Race>
    get() = raceResults.map { it.race }

  constructor(srlPlayer: SrlPlayer, races: List<Race>) :
      this(srlPlayer.id, srlPlayer.name, races.mapNotNull {
        it.raceResults
            .findLast { result -> result.player.srlName == srlPlayer.name }
      }.toMutableList())
}
