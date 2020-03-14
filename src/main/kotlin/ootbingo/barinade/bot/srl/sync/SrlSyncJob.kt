package ootbingo.barinade.bot.srl.sync

import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["ootbingo.jobs.srl-sync.enabled"], havingValue = "true")
class SrlSyncJob(private val srlHttpClient: SrlHttpClient) {

  private val logger = LoggerFactory.getLogger(SrlSyncJob::class.java)

  @Scheduled(cron = "\${ootbingo.jobs.srl-sync.cron}")
  fun execute() {
      val start = Instant.now()
      logger.info("Syncing SRL data with DB...")

      logger.info("Find all OoT races")

      val end = Instant.now()
      logger.info("SRL Sync finished. Time: ${Duration.between(start, end).standardFormat()}")
    }
}
