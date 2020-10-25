package ootbingo.barinade.bot.racing_services.srl.sync

import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPastRace

fun Set<SrlPastRace>.allPlayerNames() =
    this.map { race -> race.results.map { result -> result.player } }
        .flatten()
        .toSet()
