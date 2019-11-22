package ootbingo.barinade.bot.data.model

import ootbingo.barinade.bot.compile.Open
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Open
data class RaceResult(@Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
                      @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "race_id") var race: Race = Race(),
                      @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "player_id") var player: Player = Player(),
                      var place: Long = 0,
                      var time: Duration = Duration.ofSeconds(0),
                      var message: String = "") {

  fun isForfeit(): Boolean = time.isNegative

  override fun toString(): String = "RaceResult(time=${time.seconds})"
}
