package ootbingo.barinade.bot.statistics

import org.springframework.stereotype.Component

@Component
fun interface QueryServiceFactory {

  fun newQueryService(): QueryService
}
