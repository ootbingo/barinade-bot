package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.extensions.standardFormat
import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeLeaderboardEntry
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["ootbingo.jobs.racetime-username-update.enabled"], havingValue = "true")
class RacetimeUsernameUpdateJob(
  private val playerRepository: PlayerRepository,
  private val racetimeHttpClient: RacetimeHttpClient,
) {

  private val logger = LoggerFactory.getLogger(RacetimeUsernameUpdateJob::class.java)

  @Scheduled(cron = "\${ootbingo.jobs.racetime-username-update.cron}")
  fun execute() {

    val start = Instant.now()

    logger.info("Updating changed Racetime usernames...")

    logger.info("Loading all OoT leaderboard users...")
    val leaderboardUsers: Map<String, String> = loadLeaderboardUsers()
    logger.info("Found {} users", leaderboardUsers.size)

    if (leaderboardUsers.isEmpty()) {
      logger.warn("No users found")
      return
    }

    logger.info("Loading all users from DB")
    val dbUsers = loadDbUsers()
    logger.info("Found {} users", dbUsers.size)

    dbUsers.forEach { dbUser ->

      val leaderboardUsername: String = dbUser.racetimeId?.let { leaderboardUsers[it] } ?: return@forEach
      if (leaderboardUsername != dbUser.racetimeName) {
        logger.info("User {} renamed to {}", dbUser.racetimeName, leaderboardUsername)
        dbUser.racetimeName = leaderboardUsername
        save(dbUser)
      }
    }

    val end = Instant.now()
    logger.info("Racetime Username updates finished. Time: ${Duration.between(start, end).standardFormat()}")
  }

  private fun loadLeaderboardUsers(): Map<String, String> = try {
    racetimeHttpClient.getLeaderboards()
      .flatMap { it.rankings as Iterable<RacetimeLeaderboardEntry> }
      .map { it.user }
      .distinct()
      .associate { it.id to it.name }
  } catch (e: Exception) {
    logger.error("Failed to download leaderboards.", e)
    emptyMap()
  }

  private fun loadDbUsers(): Collection<Player> = try {
    playerRepository.findAll().toSet()
  } catch (e: Exception) {
    logger.error("Failed to players from DB.", e)
    emptySet()
  }

  private fun save(player: Player) {
    try {
      playerRepository.save(player)
    } catch (e: Exception) {
      logger.error("Failed to save updates for {}", player, e)
    }
  }
}
