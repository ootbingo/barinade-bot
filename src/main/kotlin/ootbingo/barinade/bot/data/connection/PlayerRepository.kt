package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component

@Component
interface PlayerRepository : Repository<Player, Long> {

  fun save(player: Player): Player
  fun save(players: Collection<Player>)
  fun findBySrlNameIgnoreCase(srlName: String): Player?

  @Query("""
    select new ootbingo.barinade.bot.data.model.helper.ResultInfo(res.time, r.goal, r.srlId, r.recordDate)
    from RaceResult res
    inner join fetch Race r
    on r = res.race
  
    where res.player in (
	    from Player p
	    where upper(p.srlName) = upper(:username)
    )
  
   order by r.recordDate desc
  """)
  fun findResultsForPlayer(@Param("username") username: String): List<ResultInfo>

  @Query("""
    select distinct p from Player p
    inner join fetch p.raceResults res
    inner join fetch res.race r
    
    where p.srlName in :usernames
  """)
  fun findPlayersEagerly(@Param("usernames") usernames: Collection<String>): Set<Player>
}
