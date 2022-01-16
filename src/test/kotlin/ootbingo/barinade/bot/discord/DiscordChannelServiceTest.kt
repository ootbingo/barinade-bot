package ootbingo.barinade.bot.discord

import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.restaction.ChannelAction
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

internal class DiscordChannelServiceTest {

  //<editor-fold desc="Setup">

  private val service = DiscordChannelService()

  private lateinit var returnedChannel: AtomicReference<TextChannel?>
  private var caughtException: Exception? = null

  private val guildMock = mock<Guild>()
  private val channelActionMock = mock<ChannelAction<TextChannel>>()

  @BeforeEach
  internal fun setup() {
    whenever(guildMock.createTextChannel(any(), any())).thenReturn(channelActionMock)
    givenGuildReturnsCategory(mock())
  }

  //</editor-fold>

  @Test
  internal fun throwsIfGuildIsNull() {

    whenChannelCreationIsRequested("abc", "abc", null)

    thenServiceThrows<NullPointerException>()
  }

  @Test
  internal fun throwsIfNameIsBlank() {

    whenChannelCreationIsRequested("", "abc")

    thenServiceThrows<IllegalArgumentException>()
  }

  @Test
  internal fun queriesCorrectCategory() {

    val categoryId = UUID.randomUUID().toString()

    whenChannelCreationIsRequested("abc", categoryId)

    thenCategoryWithIdIsQueried(categoryId)
  }

  @Test
  internal fun createsChannel() {

    val channelName = UUID.randomUUID().toString()
    val category = mock<Category>()

    givenGuildReturnsCategory(category)

    whenChannelCreationIsRequested(channelName, "abc")

    thenChannelCreationIsTriggered(channelName, category)
    thenChannelCreationIsSubmitted()
  }

  @Test
  internal fun returnsCreatedChannel() {

    val channel = mock<TextChannel>()

    givenActionReturnsChannel(channel)

    whenChannelCreationIsRequested("abc", "")

    thenServiceReturns(channel)
  }

  //<editor-fold desc="Given">

  private fun givenGuildReturnsCategory(category: Category) {
    whenever(guildMock.getCategoryById(any<String>())).thenReturn(category)
  }

  private fun givenActionReturnsChannel(channel: TextChannel) {
    whenever(channelActionMock.submit()).thenReturn(CompletableFuture.completedFuture(channel))
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenChannelCreationIsRequested(name: String, categoryId: String?, guild: Guild? = guildMock) {
    try {
      returnedChannel = AtomicReference(service.createChannel {
        this.name = name
        this.categoryId = categoryId
        this.guild = guild
      })
    } catch (e: Exception) {
      caughtException = e
    }
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private inline fun <reified T> thenServiceThrows() =
      assertThat(caughtException).isInstanceOf(T::class.java)

  private fun thenCategoryWithIdIsQueried(expectedCategoryId: String) =
      verify(guildMock).getCategoryById(expectedCategoryId)

  private fun thenChannelCreationIsTriggered(expectedChannelName: String, expectedCategory: Category) {
    verify(guildMock).createTextChannel(expectedChannelName, expectedCategory)
  }

  private fun thenChannelCreationIsSubmitted() {
    verify(channelActionMock).submit()
  }

  private fun thenServiceReturns(expectedChannel: TextChannel) {
    assertThat(returnedChannel).hasValue(expectedChannel)
  }

  //</editor-fold>
}
