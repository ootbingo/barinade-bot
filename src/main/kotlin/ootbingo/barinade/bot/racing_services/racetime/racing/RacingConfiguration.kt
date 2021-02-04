package ootbingo.barinade.bot.racing_services.racetime.racing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI

@Configuration
class RacingConfiguration {

  @Bean
  fun raceConnectionFactory() = object : RaceConnectionFactory {
    override fun openConnection(raceEndpoint: String) {
      RaceConnection(raceEndpoint, websocketConnector(), RaceStatusHolder())
    }
  }

  fun websocketConnector() = object : WebsocketConnector {
    override fun connect(url: String, delegate: RaceConnection): RaceWebsocketHandler {

      val handler = RaceWebsocketHandler()

      StandardWebSocketClient()
          .doHandshake(handler,
              WebSocketHttpHeaders().also { it.add("Authorization", "Bearer ${TODO()}") },
              URI.create(url))

      return handler
    }
  }
}
