package ootbingo.barinade.bot.racing_services.racetime.racing

import de.scaramangado.lily.core.communication.Dispatcher
import kotlinx.serialization.json.Json
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.racing.oauth.OAuthManager
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.URI

@Configuration
class RacingConfiguration(
    private val oauthManager: OAuthManager,
    private val dispatcher: Dispatcher,
    private val racetimeHttpClient: RacetimeHttpClient,
) {

  @Bean
  fun raceConnectionFactory(racetimeJson: Json) = RaceConnectionFactory {
    RaceConnection(it, websocketConnector(racetimeJson), RaceStatusHolder(), dispatcher, racetimeHttpClient) {
      Thread.sleep(5000)
      closeWebsocket()
    }
  }

  private fun websocketConnector(json: Json) = WebsocketConnector { url, delegate ->
    RaceWebsocketHandler(delegate, json) {
      StandardWebSocketClient()
          .execute(
              it,
              WebSocketHttpHeaders().also { h -> h.add("Authorization", "Bearer ${oauthManager.getToken()}") },
              URI.create(url),
          )
    }
  }
}
