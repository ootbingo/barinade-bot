package ootbingo.barinade.bot.srl.api

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["ootbingo.jobs.srl-sync.enabled"], havingValue = "true")
class SrlSyncJob {

  @Scheduled(cron = "\${ootbingo.jobs.srl-sync.cron}")
  fun execute() {

  }
}
