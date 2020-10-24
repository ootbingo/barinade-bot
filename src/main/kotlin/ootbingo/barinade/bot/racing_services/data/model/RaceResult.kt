package ootbingo.barinade.bot.racing_services.data.model

import ootbingo.barinade.bot.compile.Open
import java.io.Serializable
import java.time.Duration
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Open
data class RaceResult(@EmbeddedId var resultId: ResultId = ResultId(),
                      var place: Long = 0,
                      var time: Duration? = null,
                      @Enumerated(EnumType.STRING)
                      var resultType: ResultType = ResultType.FINISH) {

  fun isForfeit(): Boolean = resultType != ResultType.FINISH

  override fun toString(): String = "RaceResult(time=${time?.seconds})"

  @Embeddable
  data class ResultId(
      @ManyToOne(cascade = [], fetch = FetchType.EAGER) @JoinColumn(name = "race_id") var race: Race = Race(),
      @ManyToOne(cascade = [], fetch = FetchType.EAGER) @JoinColumn(name = "player_id") var player: Player = Player()
  ) : Serializable
}
