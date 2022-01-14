package io.github.kag0.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import java.util.UUID
import io.github.kag0.ninny.ast._
import scala.collection.compat._
import scala.util.{Failure, Success, Try}
import java.math.BigInteger
import com.typesafe.scalalogging.LazyLogging
import java.util.Base64
import scala.collection.compat.immutable.ArraySeq
import scala.util.control.NonFatal

trait FromJsonInstances
    extends VersionSpecificFromJsonInstances
    with LowPriorityFromJsonInstances
    with LazyLogging {
  implicit val stringFromJson: FromJson[String] = FromJson.fromSome {
    case string: JsonString => Success(string.value)
    case json => Failure(new JsonException(s"Expected string, got $json"))
  }

  implicit val booleanFromJson: FromJson[Boolean] = FromJson.fromSome {
    case boolean: JsonBoolean => Success(boolean.value)
    case string: JsonString =>
      Try(string.value.toBoolean).recoverWith {
        case e: IllegalArgumentException =>
          Failure(new JsonException(e.getMessage, e))
      }
    case json => Failure(new JsonException(s"Expected boolean, got $json"))
  }

  implicit val nullFromJson: FromJson[Null] = FromJson.fromSome {
    case JsonNull => Success(null)
    case json     => Failure(new JsonException(s"Expected null, got $json"))
  }

  implicit val doubleFromJson: FromJson[Double] = FromJson.fromSome {
    case number: JsonNumber => Success(number.value)
    case string: JsonString =>
      Json
        .parse(string.value, highPrecision = false)
        .recoverWith { case NonFatal(e) =>
          Failure(
            new JsonException(
              s"Failed to to parse a JSON string (${string.value}) as a number: ${e.getMessage}",
              e
            )
          )
        }
        .flatMap(_.to[Double])
    case json => Failure(new JsonException(s"Expected number, got $json"))
  }

  implicit val floatFromJson: FromJson[Float] =
    FromJson.fromSome(_.to[Double].flatMap {
      case d if d > Float.MaxValue =>
        Failure(
          new JsonException(
            s"Expected float, got $d (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case d if d < Float.MinValue =>
        Failure(
          new JsonException(
            s"Expected float, got $d (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case d => Try(d.toFloat)
    })

  implicit val sBigDecimalFromJson: FromJson[BigDecimal] = FromJson.fromSome {
    case decimal: JsonDecimal => Success(decimal.preciseValue)
    case double: JsonDouble if java.lang.Double.isFinite(double.value) =>
      logger.warn(
        "Converting JsonDouble to BigDecimal. Precision loss possible. It is recommended to use Json.parse(string, highPrecision = true) or convert to Double instead of BigDecimal"
      )
      Success(double.value)
    case string: JsonString => Try(BigDecimal(string.value))
    case json =>
      Failure(new JsonException(s"Expected number or string, got $json"))
  }

  implicit val jBigDecimalFromJson: FromJson[java.math.BigDecimal] =
    sBigDecimalFromJson.map(_.bigDecimal)

  private def wholeNumberException(number: String) =
    Failure(
      new JsonException(
        s"Expected whole number, got $number (decimal)",
        new ArithmeticException("Rounding necessary")
      )
    )

  implicit val jBigIntFromJson: FromJson[BigInteger] = FromJson.fromSome {
    case decimal: JsonDecimal if decimal.preciseValue.isWhole =>
      Success(decimal.preciseValue.bigDecimal.toBigInteger)

    case string: JsonString => Try(new BigInteger(string.value))

    case double: JsonDouble if double.value.isWhole =>
      logger.warn(
        "Converting JsonDouble to BigInt(eger). Precision loss possible. It is recommended to use Json.parse(string, highPrecision = true) or convert to Long instead of BigInt(eger)"
      )
      Success(new java.math.BigDecimal(double.value).toBigInteger)

    case n: JsonNumber => wholeNumberException(Json.render(n))

    case json =>
      Failure(new JsonException(s"Expected number or string, got $json"))
  }

  implicit val sBigIntFromJson: FromJson[BigInt] =
    jBigIntFromJson.map(BigInt(_))

  implicit val longFromJson: FromJson[Long] =
    FromJson.fromSome(_.to[BigInt].flatMap {
      case i if i > Long.MaxValue =>
        Failure(
          new JsonException(
            s"Expected long, got $i (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case i if i < Long.MinValue =>
        Failure(
          new JsonException(
            s"Expected long, got $i (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case i => Try(i.toLong)
    })

  implicit val intFromJson: FromJson[Int] =
    FromJson.fromSome(_.to[Double].flatMap {
      case d if !d.isWhole => wholeNumberException(d.toString)
      case d if d > Int.MaxValue =>
        Failure(
          new JsonException(
            s"Expected int, got $d (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case d if d < Int.MinValue =>
        Failure(
          new JsonException(
            s"Expected int, got $d (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case d => Try(d.toInt)
    })

  implicit val shortFromJson: FromJson[Short] =
    FromJson.fromSome(_.to[Int].flatMap {
      case l if l > Short.MaxValue =>
        Failure(
          new JsonException(
            s"Expected short, got $l (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case l if l < Short.MinValue =>
        Failure(
          new JsonException(
            s"Expected short, got $l (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case l => Try(l.toShort)
    })

  implicit val byteFromJson: FromJson[Byte] =
    FromJson.fromSome(_.to[Short].flatMap {
      case l if l > Byte.MaxValue =>
        Failure(
          new JsonException(
            s"Expected byte, got $l (too large)",
            new ArithmeticException("Overflow")
          )
        )
      case l if l < Byte.MinValue =>
        Failure(
          new JsonException(
            s"Expected byte, got $l (too small)",
            new ArithmeticException("Underflow")
          )
        )
      case l => Try(l.toByte)
    })

  implicit val arraySeqFromJson: FromJson[ArraySeq[Byte]] = FromJson.fromSome {
    case JsonBlob(value) => Success(value)
    case JsonString(value) =>
      Try(ArraySeq.unsafeWrapArray(Base64.getUrlDecoder.decode(value)))

    case json => Failure(new JsonException(s"Expected binary value, got $json"))
  }

  implicit val unitFromJson: FromJson[Unit] = maybeJson =>
    Success(maybeJson.map(_ => ()))

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
