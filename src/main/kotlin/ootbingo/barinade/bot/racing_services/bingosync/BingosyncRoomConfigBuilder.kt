package ootbingo.barinade.bot.racing_services.bingosync

fun bingosyncRoomConfig(block: BingosyncRoomConfigBuilder.() -> Unit) =
    BingosyncRoomConfigBuilder().apply(block).build()

class BingosyncRoomConfigBuilder(
    var name: String? = null,
    var password: String? = null,
    var variant: BingosyncRoomConfig.Variant? = null,
    var lockout: Boolean = false,
    var seed: Int? = null,
    var hideCard: Boolean = true,
) {

  fun build() =
      if (name == null || password == null || variant == null) {
        throw IllegalStateException("Name, password and variant have to be set")
      } else {
        BingosyncRoomConfig(
            name!!, password!!, variant!!, lockout, seed, hideCard
        )
      }
}

