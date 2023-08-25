package ootbingo.barinade.bot.racing_services.data.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.Duration

@Entity
data class RaceResult(
    @EmbeddedId var resultId: ResultId = ResultId(),
    var place: Long = 0,
    var time: Duration? = null,
    @Enumerated(EnumType.STRING)
    var resultType: ResultType = ResultType.FINISH,
) {

  fun isForfeit(): Boolean = resultType != ResultType.FINISH

  override fun toString(): String = "RaceResult(time=${time?.seconds})"

  @Embeddable
  data class ResultId(
      @ManyToOne(cascade = [], fetch = FetchType.EAGER) @JoinColumn(name = "race_id") var race: Race = Race(),
      @ManyToOne(cascade = [], fetch = FetchType.EAGER) @JoinColumn(name = "player_id") var player: Player = Player(),
  ) : Serializable
}
