package io.github.kag0.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}

import io.github.kag0.ninny.ast._

trait ToJsonInstances {
  implicit val stringToJson: ToSomeJson[String]   = JsonString(_)
  implicit val booleanToJson: ToSomeJson[Boolean] = JsonBoolean(_)
  implicit val nullToJson: ToSomeJson[Null]       = _ => JsonNull
  implicit val doubleToJson: ToSomeJson[Double]   = JsonNumber(_)
  implicit val intToJson: ToSomeJson[Int]         = JsonNumber(_)
  implicit val unitToJson: ToSomeJson[Unit]       = _ => JsonArray(Nil)
  implicit val jsonToJson: ToSomeJson[JsonValue]  = identity

  implicit def seqToJson[A: ToSomeJson]: ToSomeJson[Seq[A]] =
    a => JsonArray(a.map(_.toSomeJson))

  implicit def mapToJson[A: ToJson]: ToSomeJson[Map[String, A]] =
    m =>
      JsonObject(m.collect[String, JsonValue](Function.unlift {
        case (k, v) => v.toJson.map(k -> _)
      }))

  implicit def optionToJson[A: ToJson]: ToJson[Option[A]] =
    a => a.flatMap(ToJson[A].to(_))

  implicit val noneToJson: ToJson[None.type]          = _ => None
  implicit def someToJson[A: ToJson]: ToJson[Some[A]] = optionToJson[A].to(_)
  implicit def someToSomeJson[A: ToSomeJson]: ToSomeJson[Some[A]] =
    _.value.toSomeJson

  implicit val instantToJson: ToSomeJson[Instant] =
    i => JsonNumber(i.getEpochSecond)

  implicit val offsetDateTimeToJson: ToSomeJson[OffsetDateTime] =
    time => JsonString(time.toString)

  implicit val zonedDateTimeToJson: ToSomeJson[ZonedDateTime] =
    time => JsonString(time.toString)
}
