package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import com.google.gson.Gson
import com.google.gson.JsonParser
import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.extensions.description
import org.slf4j.LoggerFactory
import org.springframework.web.socket.*

@Open
class RaceWebsocketHandler(private val delegate: RaceConnection, private val gson: Gson,
                           private val handshake: (WebSocketHandler) -> Unit) : WebSocketHandler {

  private lateinit var session: WebSocketSession

  private val slug by lazy { delegate.slug }
  private val logger = LoggerFactory.getLogger(RaceWebsocketHandler::class.java)

  private var reconnectCounter = 0
  private var closed = false

  init {
    handshake(this)
  }

  fun sendMessage(message: String) {
    logger.debug("Sending chat message to $slug")
    logger.trace("'$message'")

    sendAction(SendMessage(message))
  }

  fun setGoal(goal: String) {
    logger.debug("Setting goal in $slug")
    logger.trace("'$goal'")

    sendAction(SetGoal(goal))
  }

  private fun sendAction(payload: RacetimeActionPayload) {
    val json = gson.toJson(payload.asAction())
    logger.trace(json)
    session.sendMessage(TextMessage(json))
  }

  fun disconnect() {
    logger.info("Closing session $slug...")
    closed = true
    session.close(CloseStatus.NORMAL)
  }

  //<editor-fold desc="WebSocket methods">

  override fun afterConnectionEstablished(session: WebSocketSession) {
    this.session = session
    logger.info("Connection to $slug established")
  }

  override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {

    reconnectCounter = 0

    logger.debug("Received message in $slug")
    logger.trace(message.payload.toString())

    val json = JsonParser.parseString(message.payload as String).asJsonObject

    val forwarding = when (json["type"].asString) {
      "chat.message" -> gson.fromJson(json["message"].toString(), ChatMessage::class.java)
      "race.data" -> gson.fromJson(json.toString(), RaceUpdate::class.java)
      else -> null
    }

    forwarding?.run { delegate.onMessage(this) }
  }

  override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
    logger.error("Error in $slug: ${exception.description}")
    logger.debug(null, exception)
  }

  override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
    logger.info("Connection $slug closed: ${closeStatus.reason} (${closeStatus.code})")

    if (++reconnectCounter <= 10 && closeStatus != CloseStatus.NORMAL && !closed) {
      logger.info("Reconnecting to $slug ($reconnectCounter of 10)...")
      handshake(this)
    }
  }

  override fun supportsPartialMessages() = false

  //</editor-fold>
}
