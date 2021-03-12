package ootbingo.barinade.bot.racing_services.racetime.racing

import com.google.gson.Gson
import de.scaramangado.lily.core.communication.Dispatcher
import ootbingo.barinade.bot.racing_services.racetime.racing.oauth.OAuthManager
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI

@Configuration
class RacingConfiguration(private val oauthManager: OAuthManager, private val gson: Gson,
                          private val dispatcher: Dispatcher) {

  @Bean
  fun raceConnectionFactory() = object : RaceConnectionFactory {
    override fun openConnection(raceEndpoint: String) {
      RaceConnection(raceEndpoint, websocketConnector(), RaceStatusHolder(), dispatcher) {
        Thread.sleep(5000)
        closeWebsocket()
      }
    }
  }

  @Bean
  fun websocketConnector() = object : WebsocketConnector {
    override fun connect(url: String, delegate: RaceConnection): RaceWebsocketHandler =
        RaceWebsocketHandler(delegate, gson) {
          StandardWebSocketClient()
              .doHandshake(it,
                  WebSocketHttpHeaders().also { h -> h.add("Authorization", "Bearer ${oauthManager.getToken()}") },
                  URI.create(url))
        }
  }
}
