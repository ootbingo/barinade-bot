package ootbingo.barinade.bot.srl.sync

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.connection.RaceResultRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import ootbingo.barinade.bot.srl.api.model.SrlResult
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@DataJpaTest
internal class SrlSyncJobTest(@Autowired private val playerRepository: PlayerRepository,
                              @Autowired private val raceRepository: RaceRepository,
                              @Autowired private val raceResultRepository: RaceResultRepository) {

  private val srlHttpClientMock = mock<SrlHttpClient>()
  private val job = SrlSyncJob(srlHttpClientMock, playerRepository, raceRepository, raceResultRepository)

  @Test
  internal fun importsAllOotPlayers() {

    givenPlayersInDb("Alpha" withId 1, "Beta" withId 2)
    givenRacesInDb(Race("991"))
    givenResultsInDb(result("Alpha", "991"))

    givenPlayersOnSrl("Alpha" withId 1, "Beta" withId 2, "Gamma" withId 3)
    givenRacesOnSrl(
        pastRace {
          id = 991
          goal = "race1"
          date = ZonedDateTime.from(Instant.ofEpochSecond(991))
          results {
            result {
              player = "Alpha"
              time = 551
            }
            result {
              player = "Gamma"
              time = 552
            }
          }
        },
        pastRace {
          id = 992
          goal = "race2"
          date = ZonedDateTime.from(Instant.ofEpochSecond(992))
          results {
            result {
              player = "Beta"
              time = 661
            }
            result {
              player = "Gamma"
              time = 662
            }
            result {
              player = "Alpha"
              time = 663
            }
          }
        }
    )

    whenJobIsExecuted()

    thenDbPlayerWithName("Alpha") hasRaceTimes setOf(551, 663)
    thenDbPlayerWithName("Beta") hasRaceTimes setOf(661)
    thenDbPlayerWithName("Gamma") hasId 3
    thenDbPlayerWithName("Gamma") hasRaceTimes setOf(552, 662)

    thenRaceWithId(991) hasGoal "race1"
    thenRaceWithId(991) hasDate ZonedDateTime.from(Instant.ofEpochSecond(992))
    thenRaceWithId(991) hasResults setOf("Alpha" to 663, "Beta" to 661, "Gamma" to 662)

    thenRaceWithId(992) hasGoal "race2"
    thenRaceWithId(992) hasDate ZonedDateTime.from(Instant.ofEpochSecond(991))
    thenRaceWithId(992) hasResults setOf("Alpha" to 551, "Beta" to 552)

    thenPlayerCount isEqualTo 3
    thenRaceCount isEqualTo 2
    thenResultCount isEqualTo 5
  }

  //<editor-fold desc="Given">

  private fun givenPlayersInDb(vararg players: Player) {
    playerRepository.save(players.asList())
  }

  private fun givenRacesInDb(vararg races: Race) {
    raceRepository.save(races.asList())
  }

  private fun givenResultsInDb(vararg results: RaceResult) {
    raceResultRepository.save(results.asList())
  }

  private fun givenPlayersOnSrl(vararg players: Player) {
    players.forEach {
      whenever(srlHttpClientMock.getPlayerByName(it.srlName)).thenReturn(SrlPlayer(it.srlId, it.srlName))
    }
  }

  private fun givenRacesOnSrl(vararg races: SrlPastRace) {
    whenever(srlHttpClientMock.getAllRacesOfGame(any())).thenReturn(races.toSet())
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenJobIsExecuted() = job.execute()

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenDbPlayerWithName(username: String) =
      playerRepository.findBySrlNameIgnoreCase(username) ?: throw NullPointerException()

  private infix fun Player.hasId(id: Long) {
    assertThat(this.srlId).isEqualTo(id)
  }

  private fun thenRaceWithId(id: Int) =
      raceRepository.findBySrlId(id.toString()) ?: throw NullPointerException()

  private infix fun Race.hasGoal(goal: String) {
    assertThat(this.goal).isEqualTo(goal)
  }

  private infix fun Race.hasDate(date: ZonedDateTime) {
    assertThat(this.recordDate).isEqualTo(date)
  }

  private infix fun Race.hasResults(results: Collection<Pair<String, Int>>) {
    results.forEach {
      assertThat(raceResults.lastOrNull { res -> res.player.srlName == it.first }?.time?.seconds)
          .isEqualTo(it.second)
    }

    assertThat(raceResults.size).isEqualTo(results.size)
  }

  private infix fun Player.hasRaceTimes(times: Collection<Long>) {
    assertThat(this.raceResults.map { it.time.seconds })
        .containsExactlyInAnyOrder(*times.toTypedArray())
  }

  private val thenPlayerCount: Int
    get() = playerRepository.findAll().size

  private val thenRaceCount: Int
    get() = raceRepository.findAll().size

  private val thenResultCount: Int
    get() = raceResultRepository.findAll().size

  private infix fun Int.isEqualTo(other: Int) {
    assertThat(this).isEqualTo(other)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private infix fun String.withId(id: Long): Player = Player(id, this)
  private fun result(playerName: String, raceId: String) =
      RaceResult(null, raceRepository.findBySrlId(raceId)!!, playerRepository.findBySrlNameIgnoreCase(playerName)!!)

  //<editor-fold desc="SRL Race Builder">

  private fun pastRace(builderParams: PastRaceBuilder.() -> Unit) =
      PastRaceBuilder().apply(builderParams).build()

  class PastRaceBuilder(var id: Int = -1,
                        var goal: String = "",
                        var date: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0),
                                                                          ZoneId.systemDefault())) {

    private lateinit var results: List<SrlResult>

    fun results(resultBlock: ResultsBuilder.() -> Unit) {
      results = ResultsBuilder().apply(resultBlock).build()
    }

    fun build(): SrlPastRace =
        SrlPastRace(id = id.toString(), goal = goal, date = date, numentrants = results.size.toLong(),
                    results = results)
  }

  class ResultsBuilder {

    private val results = mutableListOf<SrlResult>()

    fun result(resultParams: ResultBuilder.() -> Unit) {
      results.add(ResultBuilder().apply(resultParams).build())
    }

    fun build() = results.mapIndexed { i, r ->
      r.place = (i + 1).toLong()
      r
    }.toList()
  }

  class ResultBuilder(var player: String = "", var time: Int = 0) {
    fun build() = SrlResult(player = player, time = Duration.ofSeconds(time.toLong()))
  }

  //</editor-fold>

  //</editor-fold>
}
