package ootbingo.barinade.bot

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.NoSuchElementException

internal class BarinadeBotTest {

  @Test
  internal fun applicationContextStarts() {

    try {
      main()
    } catch (t: Throwable) {
      if (t is NoSuchElementException) {
        // Expected due to command line not being able to read in a test scenario.
        return
      }
      t.printStackTrace()
      fail<BarinadeBotTest>("Exception thrown during context creation.")
    }
  }
}
