package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.mockito.stubbing.OngoingStubbing
import java.util.*

class AntiBingoRaceRoomLogicTest {

  //<editor-fold desc="Setup">

  private val statusHolder = RaceStatusHolder()
  private val stageHolder = Holder<AntiBingoStage>(PreRaceStage)
  private val racetimeHttpClientMock = mock<RacetimeHttpClient>()
  private val delegateMock = mock<RaceRoomDelegate>()
  private val stageFactoryMock = mock<AntiBingoStageFactory>()

  private var stage by stageHolder

  private val logic =
    AntiBingoRaceRoomLogic(statusHolder, stageHolder, racetimeHttpClientMock, delegateMock, stageFactoryMock)

  @BeforeEach
  internal fun setup() {
    whenever(stageFactoryMock.raceOpenStage(any(), any())).thenReturn(mock())
    whenever(stageFactoryMock.rowPickingStage(any(), any(), any(), any(), any())).thenReturn(mock())
    whenever(stageFactoryMock.raceStartedStage(any(), any(), any())).thenReturn(mock())
  }

  //</editor-fold>

  //<editor-fold desc="Test: initialize">

  @Test
  internal fun editsRaceDuringInitialization() {

    val slug = UUID.randomUUID().toString()
    val race = RacetimeRace(slug = slug)

    whenRaceIsInitialized(race)

    thenRace(slug).isSetToManualStart()
  }

  @Test
  internal fun sendsMessageDuringInitialization() {

    whenRaceIsInitialized()

    thenMessageIsSent("Anti-Bingo initialized")
  }

  @Test
  internal fun persistsRaceDuringInitialization() {

    val race = RacetimeRace(name = UUID.randomUUID().toString())

    whenRaceIsInitialized(race)

    thenRaceIsPersisted(race)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Stages">

  //<editor-fold desc="RaceOpenStage">

  @Test
  fun raceOpenStageAfterInitializing() {

    val stageMock: RaceOpenStage = mock()

    givenStageIsReturnedByFactory(stageMock)

    whenRaceIsInitialized()

    thenStage isEqualTo stageMock
  }

  @Test
  fun raceOpenStageIsInitialized() {

    val slug = UUID.randomUUID().toString()
    val stageMock: RaceOpenStage = mock()
    val race = RacetimeRace(
      slug = slug,
      entrants = (1..4).map { RacetimeEntrant(user = RacetimeUser(name = UUID.randomUUID().toString())) },
    )

    givenStageIsReturnedByFactory(stageMock)

    whenRaceIsInitialized(race = race)

    thenStage(stageMock) isInitializedWithState AntiBingoState(
      slug,
      race.entrants.map { checkNotNull(it.user) },
      listOf()
    )
    thenStage(stageMock) isInitializedWithRace race
  }

  @Test
  internal fun sendsDmFromRaceOpenStage() {

    val message = UUID.randomUUID().toString()
    val user = RacetimeUser(id = UUID.randomUUID().toString())

    whenRaceIsInitialized()
    whenDmIsSentFromRaceOpenStage(message, user)

    thenMessageIsSent(message, expectedDirectTo = user.id)
  }

  //</editor-fold>

  //<editor-fold desc="RowPickingStage">

  @Test
  internal fun switchesToRowPickingStage() {

    val (rowPickingStageMock, whenRaceOpenStageIsComplete) = rowPickingStageMock()

    val raceMock: RacetimeRace = mock()
    val stateMock: AntiBingoState = mock()

    givenStageIsReturnedByFactory(rowPickingStageMock)

    whenRaceIsInitialized()
    whenRaceUpdateIsReceived(raceMock)
    whenRaceOpenStageIsComplete(stateMock)

    thenStage isEqualTo rowPickingStageMock
    thenStage(rowPickingStageMock) isCreatedWithState stateMock
    thenStage(rowPickingStageMock) isInitializedWithState stateMock
    thenStage(rowPickingStageMock) isInitializedWithRace raceMock
  }

  @Test
  internal fun editsRaceFromRowPickingStage() {

    val slug = UUID.randomUUID().toString()
    val race = RacetimeRace(name = "oot/$slug")
    val editsMock: RacetimeEditableRace.() -> Unit = mock()
    val (_, whenRaceOpenStageIsComplete) = rowPickingStageMock()

    whenRaceIsInitialized(race = race)
    whenRaceUpdateIsReceived(race)
    whenRaceOpenStageIsComplete(mock())
    whenRaceIsEditedFromRowPickingStage(editsMock)

    thenRace(slug).isEdited(editsMock)
  }

  @Test
  internal fun sendsMessagesFromRowPickingStage() {

    val message = UUID.randomUUID().toString()
    val actionsMock: Map<String, RacetimeActionButton> = mock()
    val (_, whenRaceOpenStageIsComplete) = rowPickingStageMock()

    whenRaceIsInitialized()
    whenRaceOpenStageIsComplete(mock())
    whenMessageIsSentFromRowPickingStage(message, actionsMock)

    thenMessageIsSent(message, expectedActions = actionsMock)
  }

  @Test
  internal fun kicksUserFromRowPickingStage() {

    val user = RacetimeUser(id = UUID.randomUUID().toString())

    whenRaceIsInitialized()
    whenRaceOpenStageIsComplete(mock())
    whenUserIsKickedFromRowPickingStage(user)

    thenUserIsKicked(user)
  }

  //</editor-fold>

  //<editor-fold desc="RaceStartedStage">

  @Test
  internal fun switchesToRaceStartedStage() {

    val (raceStartedStageMock, whenRowPickingStageIsComplete) = raceStartedStageMock()

    val raceMock: RacetimeRace = mock()
    val stateMock: AntiBingoState = mock()

    givenStageIsReturnedByFactory(raceStartedStageMock)

    whenRaceIsInitialized()
    whenRaceUpdateIsReceived(raceMock)
    whenRowPickingStageIsComplete(stateMock)

    thenStage isEqualTo raceStartedStageMock
    thenStage(raceStartedStageMock) isInitializedWithState stateMock
    thenStage(raceStartedStageMock) isInitializedWithRace raceMock
  }

  @Test
  internal fun editsRaceFromRaceStartedStage() {

    val slug = UUID.randomUUID().toString()
    val race = RacetimeRace(name = "oot/$slug")
    val editsMock: RacetimeEditableRace.() -> Unit = mock()
    val (_, whenRowPickingStageIsComplete) = raceStartedStageMock()

    whenRaceIsInitialized(race = race)
    whenRaceUpdateIsReceived(race)
    whenRowPickingStageIsComplete(mock())
    whenRaceIsEditedFromRaceStartedStage(editsMock)

    thenRace(slug).isEdited(editsMock)
  }

  @Test
  internal fun sendsMessageFromRaceStartedState() {

    val slug = UUID.randomUUID().toString()
    val race = RacetimeRace(name = "oot/$slug")
    val message = UUID.randomUUID().toString()
    val (_, whenRowPickingStageIsComplete) = raceStartedStageMock()

    whenRaceIsInitialized(race = race)
    whenRaceUpdateIsReceived(race)
    whenRowPickingStageIsComplete(mock())
    whenMessageIsSentFromRaceStartedStage(message, null)

    thenMessageIsSent(message)
  }

  @Test
  internal fun sendsDmFromRaceStartedState() {

    val slug = UUID.randomUUID().toString()
    val race = RacetimeRace(name = "oot/$slug")
    val message = UUID.randomUUID().toString()
    val user = RacetimeUser(id = UUID.randomUUID().toString())
    val (_, whenRowPickingStageIsComplete) = raceStartedStageMock()

    whenRaceIsInitialized(race = race)
    whenRaceUpdateIsReceived(race)
    whenRowPickingStageIsComplete(mock())
    whenMessageIsSentFromRaceStartedStage(message, user)

    thenMessageIsSent(message, expectedDirectTo = user.id)
  }

  //</editor-fold>

  //</editor-fold>

  //<editor-fold desc="Test: onRaceUpdate">

  @Test
  internal fun persistRaceOnUpdate() {

    val race = RacetimeRace(name = UUID.randomUUID().toString())

    whenRaceUpdateIsReceived(race)

    thenRaceIsPersisted(race)
  }

  //</editor-fold>

  //<editor-fold desc="Test: !pick">

  @Test
  internal fun sendsPickCommandToStage() {

    val stageMock = mock<RowPickingStage>()
    val chatMessageMock = mock<ChatMessage>()

    givenStage(stageMock)

    whenPickIsReceived(chatMessageMock)

    thenStage(stageMock) receivesCommand chatMessageMock
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenStageIsReturnedByFactory(stage: AntiBingoStage) {

    val stub: OngoingStubbing<AntiBingoStage> = when (stage) {
      is RaceOpenStage -> whenever(stageFactoryMock.raceOpenStage(any(), any()))
      is RowPickingStage -> whenever(stageFactoryMock.rowPickingStage(any(), any(), any(), any(), any()))
      is RaceStartedStage -> whenever(stageFactoryMock.raceStartedStage(any(), any(), any()))
      is PreRaceStage -> throw IllegalArgumentException("PreRaceStage not supported")
    }

    stub.thenReturn(stage)
  }

  private fun givenStage(newStage: AntiBingoStage) {
    stage = newStage
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRaceIsInitialized(race: RacetimeRace = RacetimeRace()) {
    logic.initialize(race)
  }

  private fun whenRaceUpdateIsReceived(race: RacetimeRace) {
    logic.onRaceUpdate(race)
  }

  private fun whenPickIsReceived(chatMessage: ChatMessage) {
    logic.commands["!pick"]?.invoke(chatMessage)
  }

  private fun whenDmIsSentFromRaceOpenStage(message: String, user: RacetimeUser) {
    val captor = argumentCaptor<(String, RacetimeUser) -> Unit>()
    verify(stageFactoryMock).raceOpenStage(any(), captor.capture())
    captor.firstValue.invoke(message, user)
  }

  private fun whenRaceOpenStageIsComplete(state: AntiBingoState) {
    val captor = argumentCaptor<(AntiBingoState) -> Unit>()
    verify(stageFactoryMock).raceOpenStage(captor.capture(), any())
    captor.firstValue.invoke(state)
  }

  private fun whenRowPickingStageIsComplete(state: AntiBingoState) {
    val captor = argumentCaptor<(AntiBingoState) -> Unit>()
    verify(stageFactoryMock).rowPickingStage(captor.capture(), any(), any(), any(), any())
    captor.firstValue.invoke(state)
  }

  private fun whenRaceIsEditedFromRowPickingStage(edits: RacetimeEditableRace.() -> Unit) {
    val captor = argumentCaptor<(RacetimeEditableRace.() -> Unit) -> Unit>()
    verify(stageFactoryMock).rowPickingStage(any(), any(), captor.capture(), any(), any())
    captor.firstValue.invoke(edits)
  }

  private fun whenMessageIsSentFromRowPickingStage(message: String, actions: Map<String, RacetimeActionButton>?) {
    val captor = argumentCaptor<(String, Map<String, RacetimeActionButton>?) -> Unit>()
    verify(stageFactoryMock).rowPickingStage(any(), any(), any(), captor.capture(), any())
    captor.firstValue.invoke(message, actions)
  }

  private fun whenRaceIsEditedFromRaceStartedStage(edits: RacetimeEditableRace.() -> Unit) {
    val captor = argumentCaptor<(RacetimeEditableRace.() -> Unit) -> Unit>()
    verify(stageFactoryMock).raceStartedStage(any(), captor.capture(), any())
    captor.firstValue.invoke(edits)
  }

  private fun whenUserIsKickedFromRowPickingStage(user: RacetimeUser) {
    val captor = argumentCaptor<(RacetimeUser) -> Unit>()
    verify(stageFactoryMock).rowPickingStage(any(), any(), any(), any(), captor.capture())
    captor.firstValue.invoke(user)
  }

  private fun whenMessageIsSentFromRaceStartedStage(message: String, user: RacetimeUser?) {
    val captor = argumentCaptor<(String, RacetimeUser?) -> Unit>()
    verify(stageFactoryMock).raceStartedStage(any(), any(), captor.capture())
    captor.firstValue.invoke(message, user)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenMessageIsSent(
    expectedMessage: String,
    expectedDirectTo: String? = null,
    expectedActions: Map<String, RacetimeActionButton>? = null,
  ) {
    verify(delegateMock).sendMessage(expectedMessage, false, expectedDirectTo, expectedActions)
  }

  private fun thenRaceIsPersisted(expectedRace: RacetimeRace) {
    assertThat(statusHolder.race).isEqualTo(expectedRace)
  }

  private fun thenUserIsKicked(expectedUser: RacetimeUser) {
    verify(delegateMock).kickUser(expectedUser)
  }

  private fun thenRace(slug: String) = RaceSlug(slug)

  private fun RaceSlug.isSetToManualStart() {

    val captor = argumentCaptor<RacetimeEditableRace.() -> Unit>()
    verify(racetimeHttpClientMock).editRace(eq(this.slug), captor.capture())
    val edits = captor.lastValue

    val testRace = mock<RacetimeEditableRace>()
    edits.invoke(testRace)
    verify(testRace).autoStart = false
  }

  private fun RaceSlug.isEdited(expectedEdits: RacetimeEditableRace.() -> Unit) {
    verify(racetimeHttpClientMock).editRace(this.slug, expectedEdits)
  }

  private val thenStage get() = stage
  private fun <T : AntiBingoStage> thenStage(stage: T) = stage

  private infix fun <T> T.isEqualTo(other: T?) {
    assertThat(this).isEqualTo(other)
  }

  private infix fun AntiBingoStage.isInitializedWithState(expectedState: AntiBingoState) {
    verify(this).initialize(eq(expectedState), any())
  }

  private infix fun RowPickingStage.isCreatedWithState(expectedState: AntiBingoState) {
    verify(stageFactoryMock).rowPickingStage(any(), eq(Holder(expectedState)), any(), any(), any())
  }

  private infix fun AntiBingoStage.isInitializedWithRace(expectedRace: RacetimeRace) {
    verify(this).initialize(any(), eq(expectedRace))
  }

  private infix fun AntiBingoStage.receivesCommand(expectedCommand: ChatMessage) {
    verify(this).handleCommand(expectedCommand)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  @JvmInline
  value class RaceSlug(val slug: String)

  private fun rowPickingStageMock() = object {

    private val raceOpenStageMock: RaceOpenStage = mock()

    init {
      givenStageIsReturnedByFactory(raceOpenStageMock)
    }

    operator fun component1(): RowPickingStage = mock()
    operator fun component2(): (AntiBingoState) -> Unit = {
      whenRaceOpenStageIsComplete(it)
    }
  }

  private fun raceStartedStageMock() = object {

    private val raceOpenStageMock: RaceOpenStage = mock()
    private val rowPickingStageMock: RowPickingStage = mock()

    init {
      givenStageIsReturnedByFactory(raceOpenStageMock)
      givenStageIsReturnedByFactory(rowPickingStageMock)
    }

    operator fun component1(): RaceStartedStage = mock()
    operator fun component2(): (AntiBingoState) -> Unit = {
      whenRaceOpenStageIsComplete(it)
      whenRowPickingStageIsComplete(it)
    }
  }

  //</editor-fold>
}
