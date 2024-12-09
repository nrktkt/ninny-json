package nrktkt.ninny.compat

import scala.annotation.nowarn
import scala.language.implicitConversions
import scala.util.{Failure, Success}
import io.circe._
import io.circe.Decoder.Result
import nrktkt.ninny.{FromJson, ToSomeJson, ToSomeJsonValue}
import nrktkt.ninny.ast._

trait NinnyToCirce {
  import CirceToNinny.asNinny

  implicit def toSomeJsonEncoder[A](implicit
      toSomeJson: ToSomeJson[A]
  ): Encoder[A] =
    new Encoder[A] {
      override def apply(a: A): Json = toSomeJson.toSome(a)
    }

  implicit def fromJsonDecoder[A](implicit
      fromJson: FromJson[A]
  ): Decoder[A] =
    new Decoder[A] {
      override def apply(c: HCursor): Result[A] =
        fromJson.from(c.root.value) match {
          case Success(value) => Right(value)
          case Failure(_) =>
            Left(DecodingFailure("decode failure", { List.empty }))
        }
    }

  implicit def asCirce(json: JsonValue): Json =
    json match {
      case JsonNull           => Json.Null
      case JsonBoolean(value) => Json.fromBoolean(value)
      case JsonString(value)  => Json.fromString(value)
      case nrktkt.ninny.ast.JsonDouble(value) => {
        if (value % 1 == 0) Json.fromInt(value.toInt)
        else Json.fromDoubleOrNull(value)
      }
      case nrktkt.ninny.ast.JsonDecimal(value) => Json.fromBigDecimal(value)
      case JsonArray(value) => Json.fromValues(value.map(asCirce))
      case nrktkt.ninny.ast.JsonObject(value) =>
        Json.fromJsonObject(io.circe.JsonObject.fromMap {
          value.map { case (k, v) =>
            (k, asCirce(v))
          }
        })
      case blob: JsonBlob => Json.fromString(nrktkt.ninny.Json.render(blob))
    }
}

object NinnyToCirce extends NinnyToCirce

trait CirceToNinny {
  import NinnyToCirce.asCirce

  implicit def encoderToSomeJson[A](implicit
      encoder: Encoder[A]
  ): ToSomeJson[A] =
    new ToSomeJsonValue[A, JsonValue] {
      override def toSome(a: A): JsonValue = encoder.apply(a)
    }

  implicit def decoderFromJson[A](implicit decoder: Decoder[A]): FromJson[A] =
    FromJson.fromSome(jsonValue => { decoder.decodeJson(jsonValue).toTry })

  @nowarn
  implicit def asNinny(json: Json): JsonValue = json match {
    case _ if json.isNull    => JsonNull
    case _ if json.isBoolean => JsonBoolean(json.asBoolean.get)
    case _ if json.isArray =>
      JsonArray(json.asArray.get.map(asNinny).toIndexedSeq)
    case _ if json.isString => JsonString(json.asString.get)
    case _ if json.isNumber =>
      json.asNumber match {
        case Some(jsonNumber) =>
          jsonNumber.toBigDecimal match {
            case Some(bigDecimal) => nrktkt.ninny.ast.JsonDecimal(bigDecimal)
          }
          nrktkt.ninny.ast.JsonNumber(jsonNumber.toDouble)
      }
    case _ if json.isObject =>
      nrktkt.ninny.ast.JsonObject(json.asObject.get.toMap.map { case (k, v) =>
        (k, asNinny(v))
      })
  }
}

object CirceToNinny extends CirceToNinny
