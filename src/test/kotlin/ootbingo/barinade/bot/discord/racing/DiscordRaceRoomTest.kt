package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState.*
import ootbingo.barinade.bot.discord.data.model.DiscordRaceState
import ootbingo.barinade.bot.discord.data.model.DiscordRaceState.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

internal class DiscordRaceRoomTest {

  //<editor-fold desc="Setup">

  private val statusMock = mock<DiscordRaceStatusHolder>()
  private val discordChannelMock = mock<TextChannel>()
  private val raceStartExecutorMock = mock<(() -> Unit) -> Unit>()
  private val waitMock = mock<WaitWrapper>()
  private val countdownServiceMock = mock<CountdownService>()

  private val readyToStartMock = mock<() -> Boolean>()

  private val room = object : DiscordRaceRoom(
      statusMock, discordChannelMock, raceStartExecutorMock, waitMock, countdownServiceMock
  ) {
    override fun readyToStart(): Boolean = readyToStartMock.invoke()
    override fun done(entrant: User): String? = null
  }

  private lateinit var reply: AtomicReference<String?>

  @BeforeEach
  internal fun setup() {
    whenever(readyToStartMock.invoke()).thenReturn(true)
    whenever(discordChannelMock.sendMessage(any<String>())).thenReturn(mock())
  }

  //</editor-fold>

  //<editor-fold desc="Test: !enter">

  @Test
  internal fun addsToHolderOnEnter() {

    val entrant = randomUser()

    givenRaceState(OPEN)

    whenEntrantEnters(entrant)

    thenEntrantIsAdded(entrant)
  }

  @Test
  internal fun repliesIfAdded() {

    val entrant = randomUser()

    givenRaceState(OPEN)
    givenHolderReturns(true)

    whenEntrantEnters(entrant)

    thenReplyMatches(Regex("^${entrant.name} entered the race\$"))
  }

  @Test
  internal fun doesNotReplyIfNotAdded() {

    givenRaceState(OPEN)
    givenHolderReturns(false)

    whenEntrantEnters(randomUser())

    thenNoReplyIsSent()
  }

  @ParameterizedTest
  @EnumSource(DiscordRaceState::class, names = ["OPEN"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNothingWhenEnteringNonOpenRoom(raceState: DiscordRaceState) {

    givenRaceState(raceState)

    whenEntrantEnters(randomUser())

    thenNoActionIsTaken()
  }

  //</editor-fold>

  //<editor-fold desc="Test: !unenter">

  @Test
  internal fun removesFromHolderOnUnenter() {

    val entrant = randomUser()

    givenRaceState(OPEN)

    whenEntrantUnenters(entrant)

    thenEntrantIsRemoved(entrant)
  }

  @Test
  internal fun repliesIfRemoved() {

    val entrant = randomUser()

    givenRaceState(OPEN)
    givenHolderReturns(true)

    whenEntrantUnenters(entrant)

    thenReplyMatches(Regex("^${entrant.name} left the race\$"))
  }

  @Test
  internal fun doesNotReplyIfNotRemoved() {

    givenRaceState(OPEN)
    givenHolderReturns(false)

    whenEntrantUnenters(randomUser())

    thenNoReplyIsSent()
  }

  @ParameterizedTest
  @EnumSource(DiscordRaceState::class, names = ["OPEN"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNothingWhenLeavingNonOpenRoom(raceState: DiscordRaceState) {

    givenRaceState(raceState)

    whenEntrantUnenters(randomUser())

    thenNoActionIsTaken()
  }

  //</editor-fold>

  //<editor-fold desc="Test: !ready">

  @Test
  internal fun setsStatusToReady() {

    val entrant = randomUser()

    givenRaceState(OPEN)

    whenEntrantReadiesUp(entrant)

    thenEntrantStatusIsChanged(entrant, READY)
  }

  @Test
  internal fun repliesIfSetToReady() {

    val entrant = randomUser()

    givenRaceState(OPEN)
    givenHolderReturns(true)

    whenEntrantReadiesUp(entrant)

    thenReplyMatches(Regex("""^${entrant.name} is ready( \(\d+ remaining\))?$"""))
  }

  @Test
  internal fun doesNotReplyIfNotSetToReady() {

    givenRaceState(OPEN)
    givenHolderReturns(false)

    whenEntrantReadiesUp(randomUser())

    thenNoReplyIsSent()
  }

  @Test
  internal fun displaysRemainingCount() {

    val notReadyCount = Random.nextInt(1, 10)

    givenRaceState(OPEN)
    givenHolderReturns(true)
    givenEntrantCounts(NOT_READY to notReadyCount)

    whenEntrantReadiesUp(randomUser())

    thenReplyMatches(Regex("""^[a-f0-9-@]* is ready \($notReadyCount remaining\)$"""))
  }

  @Test
  internal fun doesNotDisplayRemainingCountIfNooneRemains() {

    givenRaceState(OPEN)
    givenHolderReturns(true)
    givenEntrantCounts(NOT_READY to 0)

    whenEntrantReadiesUp(randomUser())

    thenReplyMatches(Regex("""[^()]*"""))
  }

  @ParameterizedTest
  @EnumSource(DiscordRaceState::class, names = ["OPEN"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNothingWhenReadyingInNonOpenRoom(raceState: DiscordRaceState) {

    givenRaceState(raceState)

    whenEntrantReadiesUp(randomUser())

    thenNoActionIsTaken()
  }

  //</editor-fold>

  //<editor-fold desc="Test: !unready">

  @Test
  internal fun setsStatusToNotReady() {

    val entrant = randomUser()

    givenRaceState(OPEN)

    whenEntrantUnreadies(entrant)

    thenEntrantStatusIsChanged(entrant, NOT_READY)
  }

  @Test
  internal fun repliesIfSetToNotReady() {

    val entrant = randomUser()

    givenRaceState(OPEN)
    givenHolderReturns(true)
    givenEntrantCounts(NOT_READY to 42)

    whenEntrantUnreadies(entrant)

    thenReplyMatches(Regex("""^${entrant.name} is not ready$"""))
  }

  @Test
  internal fun doesNotReplyIfNotSetToNotReady() {

    givenRaceState(OPEN)
    givenHolderReturns(false)

    whenEntrantUnreadies(randomUser())

    thenNoReplyIsSent()
  }

  @ParameterizedTest
  @EnumSource(DiscordRaceState::class, names = ["OPEN"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNothingWhenUnreadyingInNonOpenRoom(raceState: DiscordRaceState) {

    givenRaceState(raceState)

    whenEntrantUnreadies(randomUser())

    thenNoActionIsTaken()
  }

  //</editor-fold>

  //<editor-fold desc="Test: Race Start">

  @Test
  internal fun initiateRaceStartAfterLastPersonIsReady() {

    givenRaceState(OPEN)
    givenEntrantCounts(NOT_READY to 0, READY to 2)
    givenHolderReturns(true)

    whenEntrantReadiesUp(randomUser())

    thenRaceStartIsInitiated()
  }

  @Test
  internal fun doesNotInitiateRaceStartIfOnlyOneEntrantEntered() {

    givenRaceState(OPEN)
    givenEntrantCounts(NOT_READY to 0, READY to 1)
    givenHolderReturns(true)

    whenEntrantReadiesUp(randomUser())

    thenRaceStartIsInitiated(false)
  }

  @Test
  internal fun setsRaceStatusWhenStarting() {

    whenRaceStarts()

    thenRaceStateIsChanged(STARTING)
  }

  @Test
  internal fun waitsForRoomToBeReady() {

    givenRoomIsReadyAfterCalls(5)

    whenRaceStarts()

    thenWaitIsCalledTimes(5)
    thenRaceStartIsInitiated()
  }

  @Test
  internal fun onlyWaitsFor10Seconds() {

    givenRoomIsReadyAfterCalls(20)

    whenRaceStarts()

    thenWaitIsCalledTimes(10)
    thenRaceStartIsInitiated(false)
  }

  @Test
  internal fun countdownStartsBeforeRace() {

    whenRaceStarts()

    thenCountdownIsPostedIn(discordChannelMock)
  }

  @Test
  internal fun filenameIsPostedBeforeRace() {

    whenRaceStarts()

    thenFilenameIsPosted()
  }

  @Test
  internal fun raceStateIsSetAfterCountdown() {

    whenRaceStarts()

    thenRaceStateIsChanged(PROGRESS)
  }

  @Test
  internal fun setsStatusForAllEntrantsWhenRaceStarts() {

    whenRaceStarts()

    thenEntrantStatusIsChangedForEveryone(PLAYING)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenHolderReturns(returnValue: Boolean) {
    whenever(statusMock.addEntrant(any())).thenReturn(returnValue)
    whenever(statusMock.removeEntrant(any())).thenReturn(returnValue)
    whenever(statusMock.setStatusForEntrant(any(), any(), anyOrNull())).thenReturn(returnValue)
  }

  private fun givenRaceState(raceState: DiscordRaceState) {
    whenever(statusMock.state).thenReturn(raceState)
  }

  private fun givenEntrantCounts(vararg counts: Pair<DiscordRaceEntryState, Int>) {
    whenever(statusMock.countPerEntrantState()).thenReturn(
        counts
            .filter { it.second > 0 }
            .toMap()
    )
  }

  private fun givenRoomIsReadyAfterCalls(numberOfCalls: Int) {
    var counter = 0
    doAnswer { counter++ >= numberOfCalls }.whenever(readyToStartMock).invoke()
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenEntrantEnters(entrant: User) {
    reply = AtomicReference(room.enter(entrant))
  }

  private fun whenEntrantUnenters(entrant: User) {
    reply = AtomicReference(room.unenter(entrant))
  }

  private fun whenEntrantReadiesUp(entrant: User) {
    reply = AtomicReference(room.ready(entrant))
  }

  private fun whenEntrantUnreadies(entrant: User) {
    reply = AtomicReference(room.unready(entrant))
  }

  private fun whenRaceStarts() {

    givenRaceState(OPEN)
    givenHolderReturns(true)
    givenEntrantCounts(NOT_READY to 0, READY to 42)

    whenEntrantReadiesUp(randomUser())

    val captor = argumentCaptor<() -> Unit>()
    verify(raceStartExecutorMock, atLeast(0)).invoke(captor.capture())
    try {
      captor.lastValue.invoke()
    } catch (ignore: NoSuchElementException) {
    }
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenReplyMatches(regex: Regex) {
    assertThat(reply.get()).matches(regex.toPattern())
  }

  private fun thenNoReplyIsSent() {
    assertThat(reply).hasValue(null)
  }

  private fun thenEntrantIsAdded(expectedEntrant: User) {
    verify(statusMock).addEntrant(expectedEntrant)
  }

  private fun thenEntrantIsRemoved(expectedEntrant: User) {
    verify(statusMock).removeEntrant(expectedEntrant)
  }

  private fun thenEntrantStatusIsChanged(expectedEntrant: User, expectedStatus: DiscordRaceEntryState) {
    verify(statusMock).setStatusForEntrant(expectedEntrant, expectedStatus)
  }

  private fun thenNoActionIsTaken() {
    verify(statusMock, atLeast(0)).state
    verifyNoMoreInteractions(statusMock)
  }

  private fun thenRaceStartIsInitiated(expectStart: Boolean = true) {
    if (expectStart) verify(raceStartExecutorMock).invoke(any())
    else verifyNoInteractions(raceStartExecutorMock)
  }

  private fun thenRaceStateIsChanged(expectedState: DiscordRaceState) {
    verify(statusMock).state = expectedState
  }

  private fun thenWaitIsCalledTimes(expectedWaits: Int) {
    verify(waitMock, times(expectedWaits)).invoke(1000)
  }

  private fun thenCountdownIsPostedIn(expectedChannel: MessageChannel) {
    verify(countdownServiceMock).postCountdownInChannel(expectedChannel)
  }

  private fun thenFilenameIsPosted() {
    argumentCaptor<String>()
        .also { verify(discordChannelMock, atLeastOnce()).sendMessage(it.capture()) }
        .lastValue
        .run { assertThat(this).matches(Regex("""^Filename: [A-Z]{2}$""").toPattern()) }
  }

  private fun thenEntrantStatusIsChangedForEveryone(expectedStatus: DiscordRaceEntryState) {
    verify(statusMock).setStatusForAll(expectedStatus)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun randomUser() = mock<User>()
      .apply {
        val name = UUID.randomUUID().toString()
        whenever(idLong).thenReturn(Random.nextLong())
        whenever(asTag).thenReturn("@$name")
        whenever(this.name).thenReturn(name)
      }

  //</editor-fold>
}
