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
class SrlSyncJob(private val srlHttpClient: SrlHttpClient, private val srlPlayerImporter: SrlPlayerImporter) {

  private val logger = LoggerFactory.getLogger(SrlSyncJob::class.java)

  @Scheduled(cron = "\${ootbingo.jobs.srl-sync.cron}")
  fun execute() {
    run {
      val start = Instant.now()
      logger.info("Syncing SRL data with DB...")

      logger.info("Find all players")
      val players = srlHttpClient.getPlayerNamesOfGame("oot")
      logger.info("Found ${players.size} players")

      players.forEach {
        srlPlayerImporter.importPlayer(it)
      }

      val end = Instant.now()
      logger.info("SRL Sync finished. Time: ${Duration.between(start, end).standardFormat()}")
    }
  }
}
