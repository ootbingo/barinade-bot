package ootbingo.barinade.bot.racing_services.racetime.racing

import ootbingo.barinade.bot.racing_services.racetime.racing.oauth.OAuthManager
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI
import java.time.Instant

@Configuration
class RacingConfiguration(private val oAuthManager: OAuthManager) {

  @Bean
  fun raceConnectionFactory() = object : RaceConnectionFactory {
    override fun openConnection(raceEndpoint: String) {
      RaceConnection(raceEndpoint, websocketConnector(), RaceStatusHolder())
    }
  }

  @Bean
  fun websocketConnector() = object : WebsocketConnector {
    override fun connect(url: String, delegate: RaceConnection): RaceWebsocketHandler {

      val handler = RaceWebsocketHandler()

      StandardWebSocketClient()
          .doHandshake(handler,
              WebSocketHttpHeaders().also { it.add("Authorization", "Bearer ${oAuthManager.getToken()}") },
              URI.create(url))

      return handler
    }
  }
}
