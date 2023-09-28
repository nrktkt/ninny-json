package nrktkt.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import nrktkt.ninny.ast._
import java.math.MathContext
import java.util.UUID
import scala.collection.immutable
import scala.collection.compat.immutable.ArraySeq

trait ToJsonInstances
    extends VersionSpecificToJsonInstances
    with LowPriorityToJsonInstances {
  implicit val stringToJson: ToSomeJsonValue[String, JsonString] =
    ToJson(JsonString(_))

  implicit val booleanToJson: ToSomeJsonValue[Boolean, JsonBoolean] =
    JsonBoolean(_)

  implicit val nullToJson: ToSomeJsonValue[Null, JsonNull.type] = _ => JsonNull

  implicit val bigDecimalToJson: ToSomeJsonValue[BigDecimal, JsonDecimal] =
    JsonDecimal(_)

  implicit val bigIntToJson: ToSomeJsonValue[BigInt, JsonDecimal] = i =>
    JsonDecimal(BigDecimal(i, MathContext.UNLIMITED))

  implicit val longToJson: ToSomeJsonValue[Long, JsonDecimal] = l =>
    JsonDecimal(BigDecimal(l))

  implicit val arraySeqToJson: ToSomeJsonValue[ArraySeq[Byte], JsonBlob] =
    JsonBlob(_)

  implicit def arrayToJson[A: ToSomeJson]
      : ToSomeJsonValue[Array[A], JsonArray] =
    arr => {
      val builder = immutable.Seq.newBuilder[JsonValue]
      builder.sizeHint(arr.length)
      for (i <- 0 until arr.length) { builder += (arr(i).toSomeJson) }
      JsonArray(builder.result())
    }

  implicit def jsonToJson[J <: JsonValue]: ToSomeJson[J] = j => j

  /** represents unit as an empty JSON array. because a tuple is a heterogeneous
    * list; (5, "foo") => [5, "foo"] and unit is an empty tuple; () => []
    */
  implicit val unitToJson: ToSomeJson[Unit] = _ => JsonArray(Nil)

  implicit def mapToJson[A: ToJson]: ToSomeJson[Map[String, A]] =
    m =>
      JsonObject(m.collect(Function.unlift { case (k, v) =>
        v.toJson.map(k -> _)
      }))

  implicit def optionToJson[A: ToJson]: ToJson[Option[A]] =
    a => a.flatMap(ToJson[A].to(_))

  implicit val noneToJson: ToJson[None.type]          = _ => None
  implicit def someToJson[A: ToJson]: ToJson[Some[A]] = optionToJson[A].to(_)
  implicit def someToSomeJson[A: ToSomeJson]: ToSomeJson[Some[A]] =
    _.value.toSomeJson

  implicit def leftToSomeJson[L, R, Json <: JsonValue](implicit
      toJson: ToSomeJsonValue[L, Json]
  ): ToSomeJsonValue[Left[L, R], Json] = _.value.toSomeJson

  implicit def rightToSomeJson[L, R, Json <: JsonValue](implicit
      toJson: ToSomeJsonValue[R, Json]
  ): ToSomeJsonValue[Right[L, R], Json] = _.value.toSomeJson

  implicit def eitherToSomeJson[L: ToSomeJson, R: ToSomeJson]
      : ToSomeJson[Either[L, R]] = _.fold(_.toSomeJson, _.toSomeJson)

  implicit val instantToJson: ToSomeJson[Instant] =
    i => JsonNumber(i.getEpochSecond.toDouble)

  implicit val offsetDateTimeToJson: ToSomeJson[OffsetDateTime] =
    time => JsonString(time.toString)

  implicit val zonedDateTimeToJson: ToSomeJson[ZonedDateTime] =
    time => JsonString(time.toString)

  implicit val uuidToJson: ToSomeJsonValue[UUID, JsonString] =
    uuid => JsonString(uuid.toString)

}
object ToJsonInstances extends ToJsonInstances

trait LowPriorityToJsonInstances {
  implicit def iterableToJson[I, A](implicit
      ev: I <:< Iterable[A],
      toJson: ToSomeJson[A]
  ): ToSomeJsonValue[I, JsonArray] =
    xs => {
      val builder = immutable.Seq.newBuilder[JsonValue]
      xs.foreach(builder += _.toSomeJson)
      JsonArray(builder.result())
    }

  implicit def numericToJson[A: Numeric]: ToSomeJsonValue[A, JsonDouble] =
    a => JsonDouble(implicitly[Numeric[A]].toDouble(a))

  implicit def leftToJson[L: ToJson, R]: ToJson[Left[L, R]]   = _.value.toJson
  implicit def rightToJson[L, R: ToJson]: ToJson[Right[L, R]] = _.value.toJson
  implicit def eitherToJson[L: ToJson, R: ToJson]: ToJson[Either[L, R]] =
    _.fold(_.toJson, _.toJson)
}

class ToJsonAuto[A](val toJson: ToSomeJsonObject[A]) extends AnyVal
object ToJsonAuto                                    extends ToJsonAutoImpl
