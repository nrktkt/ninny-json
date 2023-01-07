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
  implicit val stringToJson: ToSomeJson[String] =
    ToJson(JsonString(_))

  implicit val booleanToJson: ToSomeJson[Boolean] =
    ToJson(JsonBoolean(_))

  implicit val nullToJson: ToSomeJson[Null] = ToJson(_ => JsonNull)

  implicit val bigDecimalToJson: ToSomeJson[BigDecimal] =
    ToJson(JsonDecimal(_))

  implicit val bigIntToJson: ToSomeJson.Aux[BigInt, JsonDecimal] =
    ToJson(i => JsonDecimal(BigDecimal(i, MathContext.UNLIMITED)))

  implicit val longToJson: ToSomeJson.Aux[Long, JsonDecimal] =
    ToJson(l => JsonDecimal(BigDecimal(l)))

  implicit val arraySeqToJson: ToSomeJson.Aux[ArraySeq[Byte], JsonBlob] =
    ToJson(JsonBlob(_))

  implicit def arrayToJson[A: ToSomeJson]: ToSomeJson.Aux[Array[A], JsonArray] =
    ToJson(arr => {
      val builder = immutable.Seq.newBuilder[JsonValue]
      builder.sizeHint(arr.length)
      for (i <- 0 until arr.length) { builder += (arr(i).toSomeJson) }
      JsonArray(builder.result())
    })

  implicit def jsonToJson[J <: JsonValue]: ToSomeJson[J] = ToJson(j => j)

  /** represents unit as an empty JSON array. because a tuple is a heterogeneous
    * list; (5, "foo") => [5, "foo"] and unit is an empty tuple; () => []
    */
  implicit val unitToJson: ToSomeJson[Unit] = ToJson(_ => JsonArray(Nil))

  implicit def mapToJson[A: ToJson]: ToSomeJson[Map[String, A]] =
    ToJson(m =>
      JsonObject(m.collect(Function.unlift { case (k, v) =>
        v.toJson.map(k -> _)
      }))
    )

  implicit def optionToJson[A: ToJson]: ToJson[Option[A]] =
    ToJson((a: Option[A]) => a.flatMap(ToJson[A].to(_)))

  implicit val noneToJson: ToJson[None.type] = _ => None
  implicit def someToJson[A: ToJson]: ToJson[Some[A]] =
    ToJson((some: Some[A]) => optionToJson[A].to(some))
  implicit def someToSomeJson[A: ToSomeJson]: ToSomeJson[Some[A]] =
    ToJson(_.value.toSomeJson)

  implicit def leftToJson[L: ToJson, R]: ToJson[Left[L, R]] = _.value.toJson
  implicit def rightToJson[L, R: ToJson]: ToJson[Right[L, R]] = _.value.toJson
  implicit def eitherToJson[L: ToJson, R: ToJson]: ToJson[Either[L, R]] = 
    _.fold(_.toJson, _.toJson)

  implicit val instantToJson: ToSomeJson[Instant] =
    ToJson(i => JsonNumber(i.getEpochSecond.toDouble))

  implicit val offsetDateTimeToJson: ToSomeJson[OffsetDateTime] =
    ToJson(time => JsonString(time.toString))

  implicit val zonedDateTimeToJson: ToSomeJson[ZonedDateTime] =
    ToJson(time => JsonString(time.toString))

  implicit val uuidToJson: ToSomeJson.Aux[UUID, JsonString] =
    ToJson(uuid => JsonString(uuid.toString))

}
object ToJsonInstances extends ToJsonInstances

trait LowPriorityToJsonInstances {
  implicit def iterableToJson[I, A](implicit
      ev: I <:< Iterable[A],
      toJson: ToSomeJson[A]
  ): ToSomeJson.Aux[I, JsonArray] =
    ToJson(xs => {
      val builder = immutable.Seq.newBuilder[JsonValue]
      xs.foreach(builder += _.toSomeJson)
      JsonArray(builder.result())
    })

  implicit def numericToJson[A: Numeric]: ToSomeJson.Aux[A, JsonDouble] =
    ToJson(a => JsonDouble(implicitly[Numeric[A]].toDouble(a)))
}

class ToJsonAuto[A](val toJson: ToSomeJsonObject[A]) extends AnyVal
object ToJsonAuto                                    extends ToJsonAutoImpl
