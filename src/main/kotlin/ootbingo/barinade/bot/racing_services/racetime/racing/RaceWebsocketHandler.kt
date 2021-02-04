package ootbingo.barinade.bot.racing_services.racetime.racing

import ootbingo.barinade.bot.compile.Open
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

@Open
class RaceWebsocketHandler : WebSocketHandler {

  private lateinit var session: WebSocketSession

  fun sendMessage(message: String) {
    TODO()
  }

  fun setGoal(goal: String) {
    TODO()
  }

  //<editor-fold desc="WebSocket methods">

  override fun afterConnectionEstablished(session: WebSocketSession) {
    this.session = session
  }

  override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
    TODO("Not yet implemented")
  }

  override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
    TODO("Not yet implemented")
  }

  override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
    TODO("Not yet implemented")
  }

  override fun supportsPartialMessages() = false

  //</editor-fold>
}
