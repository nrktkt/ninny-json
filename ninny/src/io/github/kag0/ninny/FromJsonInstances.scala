package io.github.kag0.ninny

import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import java.util.UUID
import io.github.kag0.ninny.ast._
import shapeless.labelled.{FieldType, field}
import shapeless.{HList, HNil, LabelledGeneric, Lazy, Witness}
import scala.collection.compat._
import scala.util.{Failure, Success, Try}
import java.math.BigInteger
import com.typesafe.scalalogging.LazyLogging
import java.util.Base64
import scala.collection.compat.immutable.ArraySeq

trait FromJsonInstances extends LowPriorityFromJsonInstances with LazyLogging {
  implicit val stringFromJson: FromJson[String] = FromJson.fromSome {
    case JsonString(value) => Success(value)
    case json              => Failure(new JsonException(s"Expected string, got $json"))
  }

  implicit val booleanFromJson: FromJson[Boolean] = FromJson.fromSome {
    case JsonBoolean(value) => Success(value)
    case JsonString(value) =>
      try { Success(value.toBoolean) }
      catch {
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
    case JsonNumber(value) => Success(value)
    case JsonString(value) => Json.parse(value).flatMap(_.to[Double])
    case json              => Failure(new JsonException(s"Expected number, got $json"))
  }

  implicit val sBigDecimalFromJson: FromJson[BigDecimal] = FromJson.fromSome {
    case JsonDecimal(value) => Success(value)
    case JsonDouble(value) =>
      logger.warn(
        "Converting JsonDouble to BigDecimal. Precision loss possible. It is recommended to use Json.parse(string, highPrecision = true) or convert to Double instead of BigDecimal"
      )
      Success(value)
    case JsonString(value) => Try(BigDecimal(value))
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
    case n: JsonNumber =>
      val preciseValue = n match {
        case JsonDecimal(value) => value
        case JsonDouble(value) =>
          logger.warn(
            "Converting JsonDouble to BigInt(eger). Precision loss possible. It is recommended to use Json.parse(string, highPrecision = true) or convert to Long instead of BigInt(eger)"
          )
          BigDecimal(value)
      }

      if (preciseValue.isWhole)
        Try(preciseValue.bigDecimal.toBigIntegerExact)
      else
        wholeNumberException(preciseValue.toString)

    case JsonString(value) => Try(new BigInteger(value))

    case json =>
      Failure(new JsonException(s"Expected number or string, got $json"))
  }

  implicit val sBigIntFromJson: FromJson[BigInt] =
    jBigIntFromJson.map(BigInt(_))

  implicit val longFromJson: FromJson[Long] =
    FromJson.fromSome(_.to[Double].flatMap {
      case d if !d.isWhole => wholeNumberException(d.toString)
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

  implicit val hNilFromJson: FromJson[HNil] = _ => Success(HNil)

  import shapeless.::

  implicit def recordFromJson[Key <: Symbol, Head, Tail <: HList](implicit
      witness: Witness.Aux[Key],
      headFromJson: Lazy[FromJson[Head]],
      tailFromJson: FromJson[Tail]
  ): FromJson[FieldType[Key, Head] :: Tail] = {
    val key  = witness.value
    val name = key.name

    FromJson.fromSome[FieldType[Key, Head] :: Tail] { json =>
      val maybeHeadJson = json / name
      for {
        head <- headFromJson.value.from(maybeHeadJson)
        tail <- tailFromJson.from(json)
      } yield field[Key](head) :: tail
    }
  }
}
object FromJsonInstances extends FromJsonInstances

trait LowPriorityFromJsonInstances {
  // this roundabout way to import compiler flag for higher kinded types avoids a deprecation warning when building for 2.13
  protected implicit lazy val hkhack = scala.languageFeature.higherKinds

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
object FromJsonAuto {

  implicit def labelledGenericFromJson[A, Head](implicit
      generic: LabelledGeneric.Aux[A, Head],
      headFromJson: Lazy[FromJson[Head]]
  ) = new FromJsonAuto[A](headFromJson.value.from(_).map(generic.from))
}
