package ootbingo.barinade.bot.misc;

import kotlin.reflect.KProperty

class Holder<T>(private var heldObject: T) {

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T = heldObject

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    heldObject = value
  }
}
