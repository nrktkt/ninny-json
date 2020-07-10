package io.github.kag0.ninny

import scala.language.dynamics

package object ast {
  sealed trait JsonValue extends Dynamic {
    def selectDynamic(name: String): Option[JsonValue] =
      this match {
        case JsonObject(values) => values.get(name)
        case _                  => None
      }

    def applyDynamic(i: Int): Option[JsonValue] =
      this match {
        case JsonArray(values) if values.length < i && i >= 0 => Some(values(i))
        case _                                                => None
      }
  }

  case class JsonObject(values: Map[String, JsonValue]) extends JsonValue
  case class JsonArray(values: Seq[JsonValue]) extends JsonValue
  case class JsonString(value: String) extends JsonValue
  case class JsonNumber(value: Double) extends JsonValue
  case class JsonBoolean(value: Boolean) extends JsonValue
  case object JsonNull extends JsonValue
}
