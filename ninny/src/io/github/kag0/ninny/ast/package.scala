package io.github.kag0.ninny

import scala.language.dynamics
import scala.collection.immutable._
import scala.collection.compat._
import scala.collection.compat.immutable.ArraySeq
import scala.reflect.runtime.universe._

package object ast {

  sealed trait JsonValue extends Any with Dynamic {
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

    override def toString = Json.render(this)

    def withUpdated = Update(this, Vector())
  }

  case class JsonObject(values: Map[String, JsonValue])
      extends AnyVal
      with JsonValue {

    def +[A: ToJson](entry: (String, A)) =
      entry._2.toJson match {
        case Some(value) =>
          this.copy(values = values + (entry._1 -> value))
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
        case Some(value) =>
          JsonObject(values - currentName + (newName -> value))
        case None => this
      }
    }
  }

  case class JsonArray(values: Seq[JsonValue]) extends AnyVal with JsonValue {

    def :+[A: ToJson](value: A) =
      value.toJson match {
        case Some(v) => this.copy(values = values :+ v)
        case None    => this
      }

    def +:[A: ToJson](value: A) =
      value.toJson match {
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

  case class JsonBlob(value: ArraySeq[Byte]) extends AnyVal with JsonValue

  sealed trait JsonNumber extends Any with JsonValue {
    // def value: Double
    // def preciseValue: BigDecimal
    // def as[N: Numeric]: N

    override def equals(obj: Any) =
      this match {
        case JsonDouble(value) =>
          obj match {
            case JsonDecimal(v) => value == v
            case JsonDouble(v)  => value == v
            case other          => value == other
          }
        case JsonDecimal(preciseValue) =>
          obj match {
            case JsonDecimal(v) => preciseValue == v
            case JsonDouble(v)  => preciseValue == v
            case other          => preciseValue == other
          }
      }
  }

  object JsonNumber {
    def apply(value: Double)      = JsonDouble(value)
    def apply(value: BigDecimal)  = JsonDecimal(value)
    def unapply(json: JsonNumber) = Some(json.value)
  }

  private[ninny] case class JsonNumberString(stringValue: String)
      extends JsonDecimal
      with JsonDouble {
    lazy val value: Double            = stringValue.toDouble
    lazy val preciseValue: BigDecimal = BigDecimal(stringValue)
    def as[N: Numeric]                = ???
    def as[N: Numeric: TypeTag] = if (typeTag[N] == typeTag[Double]) value
    else if (typeTag[N] == typeTag[BigDecimal]) preciseValue
    else implicitly[Numeric[N]].parseString(stringValue)
    /*
      if (N =:= Double) value
      else if (N =:= BigDecimal) preciseValue
      else if (N =:= java.math.BigDecimal) preciseValue.java
      else implicitly[Numeric[N]].parseString(stringValue)
     */

  }

  trait JsonDouble extends Any with JsonNumber {
    def value: Double
  }
  object JsonDouble {
    def apply(value: Double): JsonDouble = JsonDoubleWrapper(value)
    def unapply(value: JsonDouble)       = Some(value.value)
    private case class JsonDoubleWrapper(value: Double)
        extends AnyVal
        with JsonDouble
  }

  trait JsonDecimal extends Any with JsonNumber {
    def preciseValue: BigDecimal
  }
  object JsonDecimal {
    def apply(value: BigDecimal): JsonDecimal = JsonDecimalWrapper(value)
    def unapply(value: JsonDecimal)           = Some(value.preciseValue)
    private case class JsonDecimalWrapper(preciseValue: BigDecimal)
        extends AnyVal
        with JsonDecimal
  }

  sealed trait JsonBoolean extends JsonValue {
    def value = this == JsonTrue
  }
  object JsonBoolean {
    def apply(value: Boolean)      = if (value) JsonTrue else JsonFalse
    def unapply(json: JsonBoolean) = Some(json.value)
  }

  case object JsonTrue  extends JsonBoolean
  case object JsonFalse extends JsonBoolean

  case object JsonNull extends JsonValue

  case class JsonString(value: String) extends AnyVal with JsonValue
}
