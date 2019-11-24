package ootbingo.barinade.bot.srl.sync

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["ootbingo.jobs.srl-sync.enabled"], havingValue = "true")
class SrlSyncJob {

  private val logger = LoggerFactory.getLogger(SrlSyncJob::class.java)

  @Scheduled(cron = "\${ootbingo.jobs.srl-sync.cron}")
  fun execute() {
    run { }
  }
}
