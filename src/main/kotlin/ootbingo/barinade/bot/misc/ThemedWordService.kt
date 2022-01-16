package ootbingo.barinade.bot.misc

import org.springframework.stereotype.Service

@Service
class ThemedWordService(private val themedWords: () -> List<String>) {

  fun randomWord(maxLength: Int = Int.MAX_VALUE): String? =
      themedWords()
          .filter { it.length <= maxLength }
          .randomOrNull()
}
