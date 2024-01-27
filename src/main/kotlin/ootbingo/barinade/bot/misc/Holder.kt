package ootbingo.barinade.bot.misc;

import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

data class Holder<T : Any>(private var heldObject: T) {

  private val logger = LoggerFactory.getLogger(Holder::class.java)

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T = heldObject

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    logger.info("${thisRef?.let { it::class.simpleName }} set ${property.name} to ${value::class.simpleName}")
    heldObject = value
  }
}
