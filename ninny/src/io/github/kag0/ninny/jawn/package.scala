package io.github.kag0.ninny

import io.github.kag0.ninny.ast._
import org.typelevel.jawn.Facade.NoIndexFacade
import org.typelevel.jawn.FContext
import scala.collection.mutable
import scala.jdk.CollectionConverters._

package object jawn {
  sealed trait Facade extends NoIndexFacade[JsonValue] {
    val jnull                    = JsonNull
    val jfalse                   = JsonFalse
    val jtrue                    = JsonTrue
    def jstring(s: CharSequence) = JsonString(s.toString)

    def singleContext() =
      new FContext.NoIndexFContext[JsonValue] {
        private[this] var value: JsonValue = _
        def add(s: CharSequence)           = value = jstring(s)
        def add(v: JsonValue)              = value = v
        def finish()                       = value
        val isObj                          = false
      }

    def arrayContext() =
      new FContext.NoIndexFContext[JsonValue] {
        private[this] val vs     = mutable.ListBuffer.empty[JsonValue]
        def add(s: CharSequence) = vs += jstring(s)
        def add(v: JsonValue)    = vs += v
        def finish()             = JsonArray(vs.toList)
        val isObj                = false
      }

    def objectContext() =
      new FContext.NoIndexFContext[JsonValue] {
        private[this] var key: String = null
        private[this] val map         = new java.util.HashMap[String, JsonValue]

        def add(s: CharSequence) =
          if (key == null) {
            key = s.toString
          } else {
            map.put(key, jstring(s))
            key = null
          }

        def add(v: JsonValue) = {
          map.put(key, v)
          key = null
        }

        def finish() = JsonObject(new RoMap(map.asScala))
        val isObj    = true
      }

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
