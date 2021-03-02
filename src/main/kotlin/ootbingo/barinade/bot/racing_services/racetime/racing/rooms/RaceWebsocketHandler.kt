package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import com.google.gson.Gson
import com.google.gson.JsonParser
import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.extensions.description
import org.slf4j.LoggerFactory
import org.springframework.web.socket.*

@Open
class RaceWebsocketHandler(private val delegate: RaceConnection, private val gson: Gson) : WebSocketHandler {

  private lateinit var session: WebSocketSession

  private val slug = delegate.slug
  private val logger = LoggerFactory.getLogger(RaceWebsocketHandler::class.java)

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

  //<editor-fold desc="WebSocket methods">

  override fun afterConnectionEstablished(session: WebSocketSession) {
    this.session = session
    logger.info("Connection to $slug established")
  }

  override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {

    logger.debug("Received message in $slug")
    logger.trace(message.payload.toString())

    val json = JsonParser.parseString(message.payload as String).asJsonObject

    val forwarding = when (json["type"].asString) {
      "chat.message" -> "message" to ChatMessage::class.java
      "race.data" -> "race" to RaceUpdate::class.java
      else -> null
    }

    forwarding?.run { delegate.onMessage(gson.fromJson(json[first].toString(), second)) }
  }


  override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
    logger.error("Error in $slug: ${exception.description}")
    logger.debug(null, exception)
  }

  override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
    logger.info("Connection $slug closed: ${closeStatus.reason} (${closeStatus.code})")
  }

  override fun supportsPartialMessages() = false

  //</editor-fold>
}
