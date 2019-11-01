package ootbingo.barinade.bot.model

import ootbingo.barinade.bot.srl.api.model.SrlPlayer

data class Player(val srlId: Long, val name: String, val races: List<Race>) {
  constructor(srlPlayer: SrlPlayer, races: List<Race>): this(srlPlayer.id, srlPlayer.name, races)
}
