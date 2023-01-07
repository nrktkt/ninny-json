package nrktkt.ninny.compat

import play.api.libs.json.Writes
import nrktkt.ninny._
import play.api.libs.json.JsValue
import nrktkt.ninny.ast._
import play.api.libs.json._
import nrktkt.ninny

trait NinnyToPlay {
  import PlayToNinny.asNinny

  implicit def toJsonWrites[A: ToSomeJson]: Writes[A] = _.toSomeJson

  implicit def toJsonObjectWrites[A: ToSomeJsonObject]: OWrites[A] =
    a => (a.toSomeJson: JsValue).asInstanceOf[JsObject]

  implicit def fromJsonReads[A: FromJson]: Reads[A] =
    js => JsResult.fromTry(js.to[A])

  implicit def asPlay(json: JsonValue): JsValue =
    json match {
      case JsonBoolean(value) => JsBoolean(value)
      case JsonNull           => JsNull
      case JsonNumber(value)  => JsNumber(BigDecimal(value))
      case JsonString(value)  => JsString(value)
      case JsonArray(values)  => JsArray(values.map(asPlay))
      case JsonObject(values) => JsObject(values.mapValues(asPlay).toMap)
      case blob: JsonBlob     => JsString(ninny.Json.render(blob))
    }
}
object NinnyToPlay extends NinnyToPlay

trait PlayToNinny {
  import NinnyToPlay.asPlay

  implicit def writesToJson[A](implicit writes: Writes[A]): ToSomeJson[A] =
    ToJson(a => asNinny(writes.writes(a)))

  implicit def oWritesToJson[A](implicit
      writes: OWrites[A]
  ): ToSomeJsonObject[A] =
    ToJson(a => (writes.writes(a): JsonValue).asInstanceOf[JsonObject])

  implicit def readsFromJson[A](implicit reads: Reads[A]): FromJson[A] =
    FromJson.fromSome(js => JsResult.toTry(reads.reads(js)))

  implicit def asNinny(json: JsValue): JsonValue =
    json match {
      case JsBoolean(value) => JsonBoolean(value)
      case JsNull           => JsonNull
      case JsNumber(value)  => JsonNumber(value.toDouble)
      case JsString(value)  => JsonString(value)
      case JsArray(value)   => JsonArray(value.map(asNinny).toIndexedSeq)
      case JsObject(underlying) =>
        JsonObject(underlying.mapValues(asNinny).toMap)
    }
}
object PlayToNinny extends PlayToNinny

object PlayCompat extends NinnyToPlay with PlayToNinny
