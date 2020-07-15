package io.github.kag0.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}

import io.github.kag0.ninny.ast._

import scala.util.{Failure, Success, Try}

trait FromJsonInstances {
  implicit val stringFromJson: FromJson[String] = FromJson.fromSome {
    case JsonString(value) => Success(value)
    case json              => Failure(new Exception(s"Expected string, got $json"))
  }

  implicit val booleanFromJson: FromJson[Boolean] = FromJson.fromSome {
    case JsonBoolean(value) => Success(value)
    case json               => Failure(new Exception(s"Expected boolean, got $json"))
  }

  implicit val nullFromJson: FromJson[Null] = FromJson.fromSome {
    case JsonNull => Success(null)
    case json     => Failure(new Exception(s"Expected null, got $json"))
  }

  implicit val doubleFromJson: FromJson[Double] = FromJson.fromSome {
    case JsonNumber(value) => Success(value)
    case json              => Failure(new Exception(s"Expected number, got $json"))
  }

  implicit val longFromJson =
    FromJson.fromSome(_.to[Double].flatMap {
      case d if d % 1 != 0 =>
        Failure(new ArithmeticException(s"Expected long, got $d (decimal)"))
      case d if d > Long.MaxValue =>
        Failure(new ArithmeticException(s"Expected long, got $d (too large)"))
      case d if d < Long.MinValue =>
        Failure(new ArithmeticException(s"Expected long, got $d (too small)"))
      case d => Try(d.toLong)
    })

  implicit val unitFromJson: FromJson[Unit] = maybeJson =>
    Success(maybeJson.map(_ => ()))

  implicit val jsonFromJson: FromJson[JsonValue] =
    FromJson.fromSome[JsonValue](Success(_))

  implicit def seqFromJson[A: FromJson]: FromJson[Seq[A]] =
    FromJson.fromSome {
      case JsonArray(values) =>
        values
          .foldLeft[Try[List[A]]](Success(Nil))((soFar, js) =>
            soFar.flatMap(arr => js.to[A].map(_ :: arr))
          )
          .map(_.reverse)
      case json => Failure(new Exception(s"Expected array, got $json"))
    }

  implicit def optionFromJson[A: FromJson]: FromJson[Option[A]] = {
    case Some(JsonNull) => Success(None)
    case Some(json)     => FromJson[A].from(json).map(Some(_))
    case None           => Success(None)
  }

  implicit val instantFromJson: FromJson[Instant] = FromJson.fromSome(
    _.to[Long].flatMap(l => Try(Instant.ofEpochSecond(l)))
  )

  implicit val offsetDateTimeFromJson: FromJson[OffsetDateTime] =
    FromJson.fromSome(
      _.to[String].flatMap(s => Try(OffsetDateTime.parse(s)))
    )

  implicit val zonedDateTimeFromJson: FromJson[ZonedDateTime] =
    FromJson.fromSome(
      _.to[String].flatMap(s => Try(ZonedDateTime.parse(s)))
    )
}
