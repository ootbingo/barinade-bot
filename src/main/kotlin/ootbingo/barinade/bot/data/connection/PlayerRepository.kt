package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component

@Component
interface PlayerRepository : CrudRepository<Player, Long> {

  fun findBySrlNameIgnoreCase(srlName: String): Player?

  @Query("""
    select new ootbingo.barinade.bot.data.model.helper.ResultInfo(res.time, r.goal, r.id, r.datetime, res.resultType)
    from RaceResult res
    inner join fetch Race r
    on r = res.resultId.race
  
    where res.resultId.player = :player
  
   order by r.datetime desc
  """)
  fun findResultsForPlayer(player: Player): List<ResultInfo>
}
