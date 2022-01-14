package nrktkt.ninny.jsoniter

import nrktkt.ninny.ast._
import com.github.plokhotnyuk.jsoniter_scala.core.{
  JsonValueCodec,
  JsonReader,
  JsonWriter
}

object NinnyJsonValueCodec extends JsonValueCodec[JsonValue] {
  def decodeValue(in: JsonReader, default: JsonValue) = ???
  def encodeValue(x: JsonValue, out: JsonWriter) =
    x match {
      case string: JsonString => out.writeVal(string.value)
      case JsonFalse          => out.writeVal(false)
      case JsonTrue           => out.writeVal(true)
      case decimal: JsonDecimal =>
        if (decimal.preciseValue.precision == 0)
          out.writeVal(decimal.preciseValue.toBigInt)
        else
          out.writeVal(decimal.preciseValue)
      case double: JsonDouble =>
        val longValue = double.value.toLong
        if (longValue == double.value)
          out.writeVal(longValue)
        else if (java.lang.Double.isFinite(double.value))
          out.writeVal(double.value)
        else
          out.writeNonEscapedAsciiVal(double.value.toString)
      case obj: JsonObject =>
        out.writeObjectStart()
        obj.values.foreach {
          case (k, v) =>
            out.writeKey(k)
            encodeValue(v, out)
        }
        out.writeObjectEnd()
      case array: JsonArray =>
        out.writeArrayStart()
        array.values.foreach(encodeValue(_, out))
        out.writeArrayEnd()
      case blob: JsonBlob =>
        out.writeBase64UrlVal(
          blob.value.unsafeArray.asInstanceOf[Array[Byte]],
          doPadding = false
        )
      case JsonNull => out.writeNull()
    }

  def nullValue = JsonNull
}
