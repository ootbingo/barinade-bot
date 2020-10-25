package ootbingo.barinade.bot.racing_services.data

import org.springframework.stereotype.Component

@Component
class UsernameMapper(userMappingCsv: String) {

  private val userMapping: Set<Pair<String, String>> =
      userMappingCsv.lines()
          .asSequence()
          .map { it.trim() }
          .filter { !it.startsWith("#") }
          .filter { it.isNotEmpty() }
          .map { it.split(";") }
          .map { it[0] to it[1] }
          .toSet()

  fun racetimeToSrl(racetimeName: String): String =
      userMapping.findLast { it.first.equals(racetimeName, true) }?.second ?: racetimeName

  fun srlToRacetime(srlName: String): String =
      userMapping.findLast { it.second.equals(srlName, true) }?.first ?: srlName
}
