package ootbingo.barinade.bot.data.model

import java.time.Duration
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
open class RaceResult(@Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long,
                      @ManyToOne var race: Race,
                      @ManyToOne val player: Player,
                      val place: Long,
                      val time: Duration,
                      val message: String) {

  open fun isForfeit(): Boolean = time.isNegative
}
