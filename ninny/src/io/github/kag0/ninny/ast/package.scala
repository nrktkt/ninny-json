package io.github.kag0.ninny

import java.lang.Character.UnicodeBlock
import scala.language.dynamics
import scala.collection.immutable._
import scala.collection.compat._

package object ast {
  sealed trait JsonValue extends Dynamic {
    def selectDynamic(name: String)        = MaybeJsonSyntax(/(name))
    def apply(i: Int)                      = MaybeJsonSyntax(/(i))
    def applyDynamic(name: String)(i: Int) = selectDynamic(name)(i)

    def /(name: String): Option[JsonValue] =
      this match {
        case JsonObject(values) => values.get(name)
        case _                  => None
      }

    def /(i: Int): Option[JsonValue] =
      this match {
        case JsonArray(values) if values.length > i && i >= 0 => Some(values(i))
        case _                                                => None
      }

    def to[A: FromJson] = FromJson[A].from(this)

    override def toString =
      this match {
        case JsonNull           => "null"
        case JsonBoolean(value) => value.toString
        case JsonNumber(value)  => value.toString.stripSuffix(".0")
        case s: JsonString      => s.toString
        case JsonArray(values)  => values.mkString("[", ",", "]")
        case JsonObject(values) =>
          values
            .map { case (k, v) => s"${JsonString.escape(k)}:$v" }
            .mkString("{", ",", "}")
      }

    def withUpdated = Update(this, Vector())
  }

  case class JsonObject(values: Map[String, JsonValue]) extends JsonValue {

    def +(entry: (String, JsonMagnet)) =
      entry match {
        case (key, JsonMagnet(value)) =>
          this.copy(values = values + (key -> value))
        case _ => this
      }

    def ++(other: JsonObject) =
      this.copy(values = values ++ other.values)

    def ++(entries: IterableOnce[(String, JsonValue)]) =
      this.copy(values = values ++ entries)

    def -(key: String)                 = this.copy(values = values - key)
    def --(keys: IterableOnce[String]) = this.copy(values = values -- keys)

    def renameField(currentName: String, newName: String) = {
      values.get(currentName) match {
        case Some(value) => JsonObject(values - currentName + (newName -> value))
        case None => this
      }
    }
  }

  case class JsonArray(values: Seq[JsonValue]) extends JsonValue {

    def :+(value: JsonMagnet) =
      value.json match {
        case Some(v) => this.copy(values = values :+ v)
        case None    => this
      }

    def +:(value: JsonMagnet) =
      value.json match {
        case Some(v) => this.copy(values = v +: values)
        case None    => this
      }

    def :++(values: IterableOnce[JsonValue]) =
      this.copy(values = this.values ++ values)

    def ++:(values: IterableOnce[JsonValue]) =
      this.copy(values = (Seq((values.iterator ++: this.values): _*)))

    def :++(values: JsonArray): JsonArray = this :++ values.values
    def ++:(values: JsonArray): JsonArray = values.values ++: this
  }

  sealed trait JsonNumber extends JsonValue {
    def value: Double
    def equals(obj: Any): Boolean
  }

  object JsonNumber {
    def apply(value: Double)      = JsonDouble(value)
    def apply(value: BigDecimal)  = JsonDecimal(value)
    def unapply(json: JsonNumber) = Some(json.value)
  }

  case class JsonDouble(value: Double) extends JsonNumber {
    override def equals(obj: Any) =
      obj match {
        case JsonDecimal(v) => value == v
        case JsonDouble(v)  => value == v
        case other          => value == other
      }
  }

  case class JsonDecimal(preciseValue: BigDecimal) extends JsonNumber {
    def value = preciseValue.doubleValue
    override def equals(obj: Any) =
      obj match {
        case JsonDecimal(v) => preciseValue == v
        case JsonDouble(v)  => preciseValue == v
        case other          => preciseValue == other
      }
  }

  case class JsonBoolean(value: Boolean) extends JsonValue
  case object JsonNull                   extends JsonValue

  case class JsonString(value: String) extends JsonValue {
    override def toString = JsonString.escape(value)
  }

  object JsonString {
    def escape(s: String): String =
      s"${'"'}${s.map {
        case '"'  => "\\\""
        case '\\' => """\\"""
        case '/'  => """\/"""
        case '\b' => """\b"""
        case '\f' => """\f"""
        case '\n' => """\n"""
        case '\r' => """\r"""
        case '\t' => """\t"""
        case c if UnicodeBlock.of(c) != UnicodeBlock.BASIC_LATIN =>
          String.format("\\u%04x", Int.box(c))
        case c => c
      }.mkString}${'"'}"
  }
}
