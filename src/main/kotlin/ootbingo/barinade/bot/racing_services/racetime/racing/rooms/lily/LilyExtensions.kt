package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Dispatcher
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

fun Dispatcher.dispatch(chatMessage: ChatMessage): Answer<out AnswerInfo>? =
    dispatch(chatMessage.messagePlain, RacetimeMessageInfo(chatMessage))
        .orElse(null)
