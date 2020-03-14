package ootbingo.barinade.bot.srl.sync

import ootbingo.barinade.bot.srl.api.model.SrlPastRace

fun Set<SrlPastRace>.allPlayerNames() =
    this.map { race -> race.results.map { result -> result.player } }
        .flatten()
        .toSet()

fun Set<SrlPastRace>.allRaceIds() =
    this.map { race -> race.id }.toSet()
