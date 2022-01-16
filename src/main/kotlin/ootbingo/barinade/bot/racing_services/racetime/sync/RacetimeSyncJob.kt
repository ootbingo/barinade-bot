package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["ootbingo.jobs.racetime-sync.enabled"], havingValue = "true")
class RacetimeSyncJob(
    private val importerSupplier: () -> RacetimeImporter,
    private val racetimeHttpClient: RacetimeHttpClient,
) {

  private val logger = LoggerFactory.getLogger(RacetimeSyncJob::class.java)

  @Scheduled(cron = "\${ootbingo.jobs.racetime-sync.cron}")
  fun execute() {

    val start = Instant.now()
    logger.info("Syncing Racetime data with DB...")

    logger.info("Find all OoT races on Racetime...")
    val allRaces = racetimeHttpClient.getAllRaces()
    logger.info("Race data downloaded. Found {} races.", allRaces.size)

    val importer = importerSupplier()
    importer.import(allRaces)

    val end = Instant.now()
    logger.info("Racetime Sync finished. Time: ${Duration.between(start, end).standardFormat()}")
  }
}
