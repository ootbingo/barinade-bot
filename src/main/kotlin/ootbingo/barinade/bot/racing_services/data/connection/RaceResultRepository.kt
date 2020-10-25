package ootbingo.barinade.bot.racing_services.data.connection

import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
interface RaceResultRepository : CrudRepository<RaceResult, Long>
