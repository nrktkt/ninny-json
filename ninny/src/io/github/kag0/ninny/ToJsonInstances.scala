package io.github.kag0.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import io.github.kag0.ninny.ast._
import shapeless.labelled.FieldType
import shapeless.{HList, HNil, LabelledGeneric, Lazy, Witness}

import java.math.MathContext
import java.util.UUID
import scala.collection.immutable

trait ToJsonInstances extends LowPriorityToJsonInstances {
  implicit val stringToJson: ToSomeJsonValue[String, JsonString] =
    ToJson(JsonString(_))

  implicit val booleanToJson: ToSomeJsonValue[Boolean, JsonBoolean] =
    JsonBoolean(_)

  implicit val nullToJson: ToSomeJsonValue[Null, JsonNull.type] = _ => JsonNull

  implicit val bigDecimalToJson: ToSomeJsonValue[BigDecimal, JsonDecimal] =
    JsonDecimal(_)

  implicit val bigIntToJson: ToSomeJsonValue[BigInt, JsonDecimal] = i =>
    JsonDecimal(BigDecimal(i, MathContext.UNLIMITED))

  implicit val jsonToJson: ToSomeJson[JsonValue] = identity

  /**
    * represents unit as an empty JSON array.
    * because a tuple is a heterogeneous list; (5, "foo") => [5, "foo"]
    * and unit is an empty tuple; () => []
    */
  implicit val unitToJson: ToSomeJson[Unit] = _ => JsonArray(Nil)

  implicit def mapToJson[A: ToJson]: ToSomeJson[Map[String, A]] =
    m =>
      JsonObject(m.collect(Function.unlift {
        case (k, v) => v.toJson.map(k -> _)
      }))

  implicit def optionToJson[A: ToJson]: ToJson[Option[A]] =
    a => a.flatMap(ToJson[A].to(_))

  implicit val noneToJson: ToJson[None.type]          = _ => None
  implicit def someToJson[A: ToJson]: ToJson[Some[A]] = optionToJson[A].to(_)
  implicit def someToSomeJson[A: ToSomeJson]: ToSomeJson[Some[A]] =
    _.value.toSomeJson

  implicit val instantToJson: ToSomeJson[Instant] =
    i => JsonNumber(i.getEpochSecond.toDouble)

  implicit val offsetDateTimeToJson: ToSomeJson[OffsetDateTime] =
    time => JsonString(time.toString)

  implicit val zonedDateTimeToJson: ToSomeJson[ZonedDateTime] =
    time => JsonString(time.toString)

  implicit val uuidToJson: ToSomeJsonValue[UUID, JsonString] =
    uuid => JsonString(uuid.toString)

  import shapeless.::

  implicit val hNilToJson: ToSomeJsonObject[HNil] = _ => JsonObject(Map.empty)

  implicit def recordToJson[Key <: Symbol, Head, Tail <: HList](implicit
      witness: Witness.Aux[Key],
      headToJson: Lazy[ToJson[Head]],
      tailToJson: ToSomeJsonObject[Tail]
  ): ToSomeJsonObject[FieldType[Key, Head] :: Tail] = {
    val name = witness.value.name
    ToJson { record =>
      val maybeHead = headToJson.value.to(record.head)
      val tail      = tailToJson.toSome(record.tail)

      maybeHead match {
        case Some(head) => tail + (name -> head)
        case None       => tail
      }
    }
  }
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
}

class ToJsonAuto[A](val toJson: ToSomeJsonObject[A]) extends AnyVal
object ToJsonAuto {

  implicit def labelledGenericToJson[A, Head](implicit
      generic: LabelledGeneric.Aux[A, Head],
      headToJson: Lazy[ToSomeJsonObject[Head]]
  ) = new ToJsonAuto[A](a => headToJson.value.toSome(generic.to(a)))
}
