package io.github.kag0.ninny.jsoniter

import io.github.kag0.ninny.ast._
import com.github.plokhotnyuk.jsoniter_scala.core.{
  JsonValueCodec,
  JsonReader,
  JsonWriter
}

object NinnyJsonValueCodec extends JsonValueCodec[JsonValue] {
  def decodeValue(in: JsonReader, default: JsonValue) = ???
  def encodeValue(x: JsonValue, out: JsonWriter) =
    x match {
      case JsonObject(values) =>
        out.writeObjectStart()
        values.foreach {
          case (k, v) =>
            out.writeKey(k)
            encodeValue(v, out)
        }
        out.writeObjectEnd()
      case JsonDecimal(preciseValue) =>
        if (preciseValue.precision == 0)
          out.writeVal(preciseValue.toBigInt)
        else
          out.writeVal(preciseValue)
      case JsonDouble(value) =>
        val longValue = value.toLong
        if (longValue == value)
          out.writeVal(longValue)
        else
          out.writeVal(value)
      case JsonBlob(value) =>
        out.writeBase64UrlVal(
          value.unsafeArray.asInstanceOf[Array[Byte]],
          doPadding = false
        )
      case JsonString(value) => out.writeVal(value)
      case JsonFalse         => out.writeVal(false)
      case JsonTrue          => out.writeVal(true)
      case JsonArray(values) =>
        out.writeArrayStart()
        values.foreach(encodeValue(_, out))
        out.writeArrayEnd()
      case JsonNull => out.writeNull()
    }

  def nullValue = JsonNull
}
