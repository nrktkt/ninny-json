package io.github.kag0.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import java.util.UUID
import io.github.kag0.ninny.ast._
import scala.collection.compat._
import scala.util.{Failure, Success, Try}

trait FromJsonInstances
    extends AdditionalFromJsonInstances
    with LowPriorityFromJsonInstances {
  implicit val stringFromJson: FromJson[String] = FromJson.fromSome {
    case JsonString(value) => Success(value)
    case json              => Failure(new JsonException(s"Expected string, got $json"))
  }

  implicit val booleanFromJson: FromJson[Boolean] = FromJson.fromSome {
    case JsonBoolean(value) => Success(value)
    case json               => Failure(new JsonException(s"Expected boolean, got $json"))
  }

  implicit val nullFromJson: FromJson[Null] = FromJson.fromSome {
    case JsonNull => Success(null)
    case json     => Failure(new JsonException(s"Expected null, got $json"))
  }

  implicit val doubleFromJson: FromJson[Double] = FromJson.fromSome {
    case JsonNumber(value) => Success(value)
    case json              => Failure(new JsonException(s"Expected number, got $json"))
  }

  implicit val longFromJson: FromJson[Long] =
    FromJson.fromSome(_.to[Double].flatMap {
      case d if d % 1 != 0 =>
        Failure(
          new JsonException(
            s"Expected long, got $d (decimal)",
            new ArithmeticException("Rounding necessary")
          )
        )
      case d if d > Long.MaxValue =>
        Failure(
          new JsonException(
            s"Expected long, got $d (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case d if d < Long.MinValue =>
        Failure(
          new JsonException(
            s"Expected long, got $d (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case d => Try(d.toLong)
    })

  implicit val intFromJson: FromJson[Int] =
    FromJson.fromSome(_.to[Long].flatMap {
      case l if l > Int.MaxValue =>
        Failure(
          new JsonException(
            s"Expected int, got $l (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case l if l < Int.MinValue =>
        Failure(
          new JsonException(
            s"Expected int, got $l (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case l => Try(l.toInt)
    })

  implicit val unitFromJson: FromJson[Unit] = _ => Success(())

  implicit def jsonFromJson[J <: JsonValue]: FromJson[J] =
    FromJson.fromSome[J](j => Try(j.asInstanceOf[J]))

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

  implicit val uuidFromJson: FromJson[UUID] = FromJson.fromSome(
    _.to[String].flatMap(s => Try(UUID.fromString(s)))
  )

}
object FromJsonInstances extends FromJsonInstances

trait LowPriorityFromJsonInstances {
  // this roundabout way to import compiler flag for higher kinded types avoids a deprecation warning when building for 2.13
  protected implicit lazy val hkhack: languageFeature.higherKinds.type =
    scala.languageFeature.higherKinds

  implicit def collectionFromJson[F[_], A](implicit
      factory: Factory[A, F[A]],
      A: FromJson[A]
  ): FromJson[F[A]] =
    FromJson.fromSome {
      case JsonArray(values) =>
        val builder = factory.newBuilder
        builder.sizeHint(values)
        values
          .foldLeft[Try[Vector[A]]](Success(Vector.empty[A]))((soFar, js) =>
            soFar.flatMap(arr => js.to[A].map(arr :+ _))
          )
          .map { out =>
            builder ++= out
            builder.result()
          }

      case json => Failure(new JsonException(s"Expected array, got $json"))
    }
}

class FromJsonAuto[A](val fromJson: FromJson[A]) extends AnyVal
object FromJsonAuto                              extends FromJsonAutoImpl
