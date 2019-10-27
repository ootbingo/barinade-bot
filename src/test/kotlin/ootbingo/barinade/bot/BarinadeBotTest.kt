package ootbingo.barinade.bot

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.NoSuchElementException

internal class BarinadeBotTest {

  @Test
  internal fun applicationContextStarts() {

    try {
      main()
    } catch (e: NoSuchElementException) {
      // Expected due to command line not being able to read in a test scenario.
    }
  }

  @Test
  internal fun jenkins() {
    Assertions.fail<Unit>("Test case 'jenkins' not implemented.")
  }
}
