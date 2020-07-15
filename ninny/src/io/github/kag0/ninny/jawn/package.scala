package io.github.kag0.ninny

import io.github.kag0.ninny.ast._
import org.typelevel.jawn.Facade.SimpleFacade

package object jawn {
  implicit object Facade extends SimpleFacade[JsonValue] {
    def jarray(vs: List[JsonValue])         = JsonArray(vs)
    def jobject(vs: Map[String, JsonValue]) = JsonObject(vs)
    val jnull                               = JsonNull
    val jfalse                              = JsonBoolean(false)
    val jtrue                               = JsonBoolean(true)
    def jstring(s: CharSequence)            = JsonString(s.toString)

    def jnum(s: CharSequence, decIndex: Int, expIndex: Int) =
      JsonNumber(s.toString.toDouble)
  }
}
