package nrktkt.ninny

import scala.language.dynamics
import scala.collection.immutable._
import scala.collection.compat._
import scala.collection.compat.immutable.ArraySeq

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

    /** Do not use this. Use [[Json.render]] instead. This method may return a
      * human-readable representation of the AST in the future.
      */
    override def toString = Json.render(this)

    def withUpdated = Update(this, Vector())
  }

  case class JsonObject(values: Map[String, JsonValue])
      extends AnyVal
      with JsonValue {

    def mapNames(f: String => String) = JsonObject(values.map { case (k, v) =>
      f(k) -> v
    })

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

    /** Deep merge another object into this one.
      *
      * @return
      *   This object plus all the fields on that object. If a field is an
      *   object in both this and that, the objects will be merged recursively.
      *   If a field is present on both, and the values are not both objects,
      *   then that field will overwrite this one.
      */
    def +++(that: JsonObject): JsonObject = {
      val sharedKeys = this.values.keySet.intersect(that.values.keySet)
      val merged = sharedKeys.view
        .map(key =>
          key -> (that.values(key) match {
            case thatObject: JsonObject =>
              this.values(key) match {
                case thisObject: JsonObject => thisObject +++ thatObject
                case _                      => thatObject
              }
            case thatValue => thatValue
          })
        )

      JsonObject(
        (this.values -- sharedKeys) ++ (that.values -- sharedKeys) ++ merged
      )
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
    def value: Double
  }

  object JsonNumber {
    def apply(value: Double)      = JsonDouble(value)
    def apply(value: BigDecimal)  = JsonDecimal(value)
    def unapply(json: JsonNumber) = Some(json.value)
  }

  case class JsonDouble(value: Double) extends JsonNumber {

    override def equals(obj: Any) =
      obj match {
        case JsonDecimal(v) => v == value
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
