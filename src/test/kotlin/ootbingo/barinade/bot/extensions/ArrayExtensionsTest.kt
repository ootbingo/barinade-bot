package ootbingo.barinade.bot.extensions

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ArrayExtensionsTest {

  @Test
  internal fun partition1_1() {

    val input = listOf(1)

    val output = input.allPartitions()

    assertThat(output)
        .containsExactlyInAnyOrder(listOf(listOf(1)))
  }

  @Test
  internal fun partition2_2() {

    val input = listOf(1, 2)

    val output = input.allPartitions()

    assertThat(output)
        .containsExactlyInAnyOrder(listOf(listOf(1), listOf(2)),
                                   listOf(listOf(1, 2)))
  }

  @Test
  internal fun partition2_1() {

    val input = listOf(1, 2)

    val output = input.allPartitions(1)

    assertThat(output)
        .containsExactlyInAnyOrder(listOf(listOf(1), listOf(2)))
  }

  @Test
  internal fun partition3_3() {

    val input = listOf(1, 2, 3)

    val output = input.allPartitions()

    assertThat(output)
        .containsExactlyInAnyOrder(listOf(listOf(1), listOf(2), listOf(3)),
                                   listOf(listOf(1), listOf(2, 3)),
                                   listOf(listOf(2), listOf(1, 3)),
                                   listOf(listOf(1, 2), listOf(3)),
                                   listOf(listOf(1, 2, 3)))
  }

  @Test
  internal fun partition3_2() {

    val input = listOf(1, 2, 3)

    val output = input.allPartitions(2)

    assertThat(output)
        .containsExactlyInAnyOrder(listOf(listOf(1), listOf(2), listOf(3)),
                                   listOf(listOf(1), listOf(2, 3)),
                                   listOf(listOf(2), listOf(1, 3)),
                                   listOf(listOf(1, 2), listOf(3)))
  }

  @Test
  internal fun partition3_1() {

    val input = listOf(1, 2, 3)

    val output = input.allPartitions(1)

    assertThat(output)
        .containsExactlyInAnyOrder(listOf(listOf(1), listOf(2), listOf(3)))
  }

  @Test
  internal fun partition7_3() {

    assertThat(listOf(1, 2, 3, 4, 5, 6, 7).allPartitions(3))
        .hasSize(652)
  }

  @Test
  internal fun partition12_4() {

    assertThat(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).allPartitions(4))
        .hasSize(3305017)
  }

  @Test
  internal fun exceptionForTooBigPartitionSize() {

    assertThatCode { (1..2).toList().allPartitions(5) }
        .isExactlyInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  internal fun exceptionForTooBigList() {

    assertThatCode { (1..13).toList().allPartitions() }
        .isExactlyInstanceOf(IllegalArgumentException::class.java)
  }
}
