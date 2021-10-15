package io.github.kag0.ninny

import io.github.kag0.ninny.ast._
import org.typelevel.jawn.Facade.SimpleFacade

package object jawn {
  sealed trait Facade extends SimpleFacade[JsonValue] {
    def jarray(vs: List[JsonValue])         = JsonArray(vs)
    def jobject(vs: Map[String, JsonValue]) = JsonObject(vs)
    val jnull                               = JsonNull
    val jfalse                              = JsonFalse
    val jtrue                               = JsonTrue
    def jstring(s: CharSequence)            = JsonString(s.toString)
  }

  object DecimalFacade extends Facade {
    def jnum(s: CharSequence, decIndex: Int, expIndex: Int) =
      JsonDecimal(BigDecimal(s.toString))
  }

  object DoubleFacade extends Facade {
    def jnum(s: CharSequence, decIndex: Int, expIndex: Int) = 
    JsonDouble(s.toString.toDouble)
  }
}
