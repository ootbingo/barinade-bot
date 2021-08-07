package ootbingo.barinade.bot.racing_services.data

import com.nhaarman.mockitokotlin2.*
import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

internal class PlayerHelperTest {

  //<editor-fold desc="Setup">

  private val playerRepositoryMock = mock<PlayerRepository>()
  private val usernameMapperMock = mock<UsernameMapper>()
  private val helper = PlayerHelper(playerRepositoryMock, usernameMapperMock)

  private val dbPlayers = mutableListOf<Player>()

  @BeforeEach
  internal fun setup() {

    doAnswer {
      dbPlayers.lastOrNull { p -> it.getArgument<String>(0).equals(p.srlName, true) }
    }.whenever(playerRepositoryMock).findBySrlNameIgnoreCase(any())

    doAnswer {
      dbPlayers.lastOrNull { p -> it.getArgument<String>(0).equals(p.racetimeName, true) }
    }.whenever(playerRepositoryMock).findByRacetimeNameIgnoreCase(any())

    doAnswer { it.getArgument<Player>(0) }.whenever(playerRepositoryMock).save(any<Player>())

    doAnswer { it.getArgument<String>(0) }.whenever(usernameMapperMock).racetimeToSrl(any())
    doAnswer { it.getArgument<String>(0) }.whenever(usernameMapperMock).srlToRacetime(any())
  }

  //</editor-fold>

  //<editor-fold desc="General Player Queries">

  @Test
  internal fun returnsNullIfPlayerNotKnown() {

    givenPlayersInDb()

    assertThat(helper.getPlayerByName("some name")).isNull()
  }

  @Test
  internal fun readsPlayerFromDbByRacetimeNameFirst() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersInDb(Player(playerId, 0, "", "srlName", playerName),
        Player(-1, 0, "", playerName, "racetimeName"))

    val actualPlayer = helper.getPlayerByName(playerName)!!

    assertThat(actualPlayer.id).isEqualTo(playerId)
  }

  @Test
  internal fun readsPlayerFromDbBySrlNameSecond() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersInDb(Player(playerId, 0, "", playerName, "racetimeName"))

    val actualPlayer = helper.getPlayerByName(playerName)!!

    assertThat(actualPlayer.id).isEqualTo(playerId)
  }

  @Test
  internal fun redirectsResultQuery() {

    val player = Player(42)
    val playerRepositoryMock = mock<PlayerRepository>()
    val expectedResult = listOf<ResultInfo>()

    whenever(playerRepositoryMock.findResultsForPlayer(player)).thenReturn(expectedResult)

    assertThat(helper.findResultsForPlayer(player)).isEqualTo(expectedResult)
  }

  //</editor-fold>

  //<editor-fold desc="Racetime Player Queries">

  @Test
  internal fun findsExistingRacetimePlayer() {

    val racetimeId = UUID.randomUUID().toString()
    val racetimePlayer = Player(racetimeId = racetimeId)

    givenPlayersInDb(racetimePlayer)

    whenRacetimePlayerIsQueried(racetimeId)

    thenPlayer isEqualTo racetimePlayer
  }

  @Test
  internal fun savesNewPlayerIfRacetimePlayerNotFound() {

    val racetimeId = UUID.randomUUID().toString()
    val racetimeName = UUID.randomUUID().toString()

    whenRacetimePlayerIsQueried(racetimeId, racetimeName)

    thenPlayer hasRacetimeId racetimeId
    thenPlayer hasRacetimeName racetimeName
    thenPlayer.wasSaved()
  }

  @Test
  internal fun updatesSrlPlayerForNewRacetimePlayer() {

    val racetimeId = UUID.randomUUID().toString()
    val racetimeName = UUID.randomUUID().toString()

    val srlId = Random.nextLong()
    val srlName = racetimeName.toUpperCase()

    givenPlayersInDb(Player(srlId = srlId, srlName = srlName))

    whenRacetimePlayerIsQueried(racetimeId, racetimeName)

    thenPlayer hasRacetimeId racetimeId
    thenPlayer hasRacetimeName racetimeName
    thenPlayer hasSrlId srlId
    thenPlayer hasSrlName srlName
    thenPlayer.wasSaved()
  }

  @Test
  internal fun updatesSrlPlayerForNewRacetimePlayerWithMappedName() {

    val racetimeId = UUID.randomUUID().toString()
    val racetimeName = UUID.randomUUID().toString()

    val srlId = Random.nextLong()
    val srlName = UUID.randomUUID().toString()

    givenPlayersInDb(Player(srlId = srlId, srlName = srlName))
    given(racetimeName) isRacetimeNameOf srlName

    whenRacetimePlayerIsQueried(racetimeId, racetimeName)

    thenPlayer hasRacetimeId racetimeId
    thenPlayer hasRacetimeName racetimeName
    thenPlayer hasSrlId srlId
    thenPlayer hasSrlName srlName
    thenPlayer.wasSaved()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenPlayersInDb(vararg players: Player) = dbPlayers.addAll(players)

  private fun given(string: String) = string

  private infix fun String.isSrlNameOf(racetimeName: String) {
    whenever(usernameMapperMock.srlToRacetime(this)).thenReturn(racetimeName)
  }

  private infix fun String.isRacetimeNameOf(srlName: String) {
    whenever(usernameMapperMock.racetimeToSrl(this)).thenReturn(srlName)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRacetimePlayerIsQueried(racetimeId: String, racetimeName: String = "") {
    thenPlayer = helper.getPlayerFromRacetimeId(racetimeId, racetimeName)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private var thenPlayer: Player? = null

  private infix fun Player?.isEqualTo(expectedPlayer: Player?) =
      assertThat(this).isEqualTo(expectedPlayer)

  private infix fun Player?.hasRacetimeId(expectedId: String) =
      assertThat(this?.racetimeId).withFailMessage("Wrong Racetime ID").isEqualTo(expectedId)

  private infix fun Player?.hasRacetimeName(expectedName: String) =
      assertThat(this?.racetimeName).withFailMessage("Wrong Racetime Name").isEqualTo(expectedName)

  private infix fun Player?.hasSrlId(expectedId: Long) =
      assertThat(this?.srlId).withFailMessage("Wrong SRL ID").isEqualTo(expectedId)

  private infix fun Player?.hasSrlName(expectedName: String) =
      assertThat(this?.srlName).withFailMessage("Wrong SRL Name").isEqualTo(expectedName)

  private fun Player?.wasSaved() =
      if (this != null) {
        verify(playerRepositoryMock).save(this)
      } else {
        fail("null cannot be saved")
      }

  //</editor-fold>
}
