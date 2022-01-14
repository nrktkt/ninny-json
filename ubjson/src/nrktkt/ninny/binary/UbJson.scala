package nrktkt.ninny.binary

import scala.collection.immutable.ArraySeq
import scala.util.Try
import nrktkt.ninny.ast._
import nrktkt.ninny.Json
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.IndexedSeqView
import java.nio.ByteBuffer
import scala.util.Success
import scala.annotation.tailrec
import scala.util.Failure
import nrktkt.ninny.JsonException
import java.nio.charset.StandardCharsets
import java.util.Arrays

object UbJson extends LowConflictMarkers {
  private type ISV = IndexedSeqView[Byte]

  def render(json: JsonValue): ArraySeq[Byte] = renderView(json).to(ArraySeq)

  private val asciiEncoder = StandardCharsets.US_ASCII.newEncoder

  private def getBytesForNum(n: Int, f: ByteBuffer => ByteBuffer) =
    ArraySeq
      .unsafeWrapArray(
        f(
          ByteBuffer.allocate(n)
        ).array
      )
      .view

  private def renderView(json: JsonValue): ISV = {
    json match {
      case JsonNull       => ArraySeq[Byte](Null).view
      case JsonBoolean(b) => ArraySeq[Byte](if (b) True else False).view

      case JsonDouble(value) if value.isValidByte =>
        ArraySeq[Byte](Int8, value.toByte).view

      case JsonDouble(value) if value.isWhole && 0 <= value && value <= 255 =>
        ArraySeq[Byte](UInt8, value.toByte).view

      case JsonDouble(value) if value.isValidShort =>
        getBytesForNum(2, _.putShort(value.toShort))
          .prepended(Int16)

      case JsonDouble(value) if value.isValidInt =>
        getBytesForNum(4, _.putInt(value.toInt))
          .prepended(Int32)

      case JsonDouble(value) if {
            val l = value.toLong;
            l.toDouble == value && (l != Long.MaxValue || value == Long.MaxValue)
          } =>
        getBytesForNum(8, _.putLong(value.toLong)).prepended(Int64)

      case JsonDouble(value) if value.toFloat.toDouble == value =>
        getBytesForNum(4, _.putFloat(value.toFloat)).prepended(Float32)

      case JsonDouble(value) =>
        getBytesForNum(8, _.putDouble(value)).prepended(Float64)

      case d: JsonDecimal =>
        // render to a JSON spec number string, then add the UBJSON length prefix
        val bytes = renderView(JsonString(Json.render(d))).drop(1)
        ArraySeq[Byte](BigNum).view.concat(bytes)

      case JsonString(value)
          if value.length == 1 && asciiEncoder.canEncode(value.head) =>
        ArraySeq[Byte](Markers.Char, value.head.toByte).view

      case JsonString(value) =>
        val bytes = value.getBytes(UTF_8)
        renderView(JsonDouble(bytes.length))
          .prepended(Markers.String)
          .concat(bytes)

      case JsonBlob(value) =>
        ArraySeq[Byte](ArrStart, Type, UInt8, Count).view
          .concat(renderView(JsonDouble(value.length)))
          .concat(value.view)

      case JsonArray(values) =>
        values
          .map(renderView)
          .fold(ArraySeq.empty[Byte].view)(_.concat(_))
          .prepended(ArrStart)
          .appended(ArrEnd)

      case JsonObject(values) =>
        values
          .map {
            case (name, value) =>
              renderView(JsonString(name)).drop(1).concat(renderView(value))
          }
          .fold(ArraySeq.empty[Byte].view)(_.concat(_))
          .prepended(ObjStart)
          .appended(ObjEnd)
    }
  }

  def parse(bytes: ArraySeq[Byte]): Try[JsonValue] = parse(bytes.view).map(_._1)

  def parse(bytes: ISV): Try[(JsonValue, ISV)] =
    bytes match {
      case bObject(obj, rest)    => Success(obj, rest)
      case bArray(arr, rest)     => Success(arr, rest) // also binary
      case Null +:: rest         => Success(JsonNull, rest)
      case NoOp +:: rest         => parse(rest)
      case True +:: rest         => Success(JsonTrue, rest)
      case False +:: rest        => Success(JsonFalse, rest)
      case bString(string, rest) => Success(string, rest)
      case bNumber(value, rest)  => Success(value, rest)
      case Markers.Char +:: char +:: rest =>
        Try((JsonString(char.toChar.toString), rest))

      case marker +:: _ =>
        Failure(
          new JsonException(
            s"Invalid UBJSON value with marker [${marker.toChar}]"
          )
        )

      case _ =>
        Failure(
          new JsonException("UBJSON value ended unexpectedly or had no marker")
        )
    }

  object bString {
    def unapply(bytes: ISV): Option[(JsonString, ISV)] =
      bytes match {
        case Markers.String +:: bNumber(JsonNumber(d), tail) =>
          val i           = d.toInt
          val stringBytes = tail.take(i)
          val rest        = tail.drop(i) // .splitAt doesn't return ISV

          Try(
            (JsonString(new String(stringBytes.toArray, UTF_8)), rest)
          ).toOption

        case _ => None
      }
  }

  object bNumber {
    private def getNumFromBtyes(bytes: ISV, n: Int, f: ByteBuffer => Double) =
      (JsonDouble(f(ByteBuffer.wrap(bytes.take(n).toArray))), bytes.drop(n))

    def unapply(bytes: ISV): Option[(JsonNumber, ISV)] =
      bytes match {
        case UInt8 +:: value +:: tail =>
          Some(JsonDouble(java.lang.Byte.toUnsignedInt(value)), tail)

        case Int8 +:: value +:: tail =>
          Some(JsonDouble(value.toDouble), tail)

        case Int16 +:: tail if tail.length >= 2 =>
          Some(getNumFromBtyes(tail, 2, _.getShort))

        case Int32 +:: tail if tail.length >= 4 =>
          Some(getNumFromBtyes(tail, 4, _.getInt))

        case Int64 +:: tail if tail.length >= 8 =>
          Some(getNumFromBtyes(tail, 8, _.getLong))

        case Float32 +:: tail if tail.length >= 4 =>
          Some(
            JsonDouble(ByteBuffer.wrap(tail.take(4).toArray).getFloat),
            tail.drop(4)
          )

        case Float64 +:: tail if tail.length >= 8 =>
          Some(
            JsonDouble(ByteBuffer.wrap(tail.take(8).toArray).getDouble),
            tail.drop(8)
          )

        case BigNum +:: bNumber(JsonNumber(value), tail) =>
          val length = value.toInt
          Try(new String(tail.take(length).toArray, UTF_8))
            .flatMap(Json.parse(_, highPrecision = true).to[JsonNumber])
            .map(_ -> tail.drop(length))
            .toOption

        case _ => None
      }
  }

  private object TypeMarker {
    def unapply(bytes: ISV) =
      bytes match {
        case Markers.Type +:: marker +:: tail => Some(Some(marker), tail)
        case bytes                            => Some(None, bytes)
      }
  }

  object bArray {
    def unapply(bytes: ISV): Option[(JsonValue, ISV)] =
      bytes match {
        // typed arrays of unsigned bytes are blobs
        case ArrStart +:: '$' +:: UInt8 +:: '#' +:: bNumber(
              JsonNumber(n),
              tail
            ) =>
          val length = n.toInt
          Some(
            JsonBlob(ArraySeq.unsafeWrapArray(tail.take(length).toArray)),
            tail.drop(length)
          )

        // length prefixed and optionally typed
        case ArrStart +:: TypeMarker(
              maybeMarker,
              Markers.Count +:: bNumber(
                JsonNumber(n),
                tail
              )
            ) =>
          Iterable
            .iterate(Try((Vector.empty[JsonValue], tail)), n.toInt + 1) {
              _.flatMap {
                case (values, rest) =>
                  parse(maybeMarker.map(rest.prepended).getOrElse(rest)).map {
                    case (field, restrest) => (values :+ field, restrest)
                  }
              }
            }
            .lastOption
            .flatMap {
              case Success((values, rest)) => Some(JsonArray(values), rest)
              case Failure(_)              => None
            }

        case ArrStart +:: array =>
          scan(array).toOption.map {
            case (arr, rest) => (JsonArray(arr), rest)
          }

        case _ => None
      }

    // parse the values in an array until the end marker is reached
    @tailrec private def scan(
        bs: ISV,
        values: Seq[JsonValue] = Seq.empty
    ): Try[(Seq[JsonValue], ISV)] =
      bs match {
        case ArrEnd +:: rest => Success((values, rest))

        case empty if empty.isEmpty =>
          Failure(
            new IndexOutOfBoundsException("Array ended unexpectedly")
          )

        case rest =>
          parse(rest) match {
            case Success((value, r)) => scan(r, values :+ value)
            case Failure(ex)         => Failure(ex)
          }
      }
  }

  object bObject {
    def unapply(bytes: ISV): Option[(JsonObject, ISV)] =
      bytes match {
        case ObjStart +:: TypeMarker(
              maybeMarker,
              Markers.Count +:: bNumber(
                JsonNumber(n),
                tail
              )
            ) =>
          Iterable
            .iterate(Try((Map.empty[String, JsonValue], tail)), n.toInt + 1) {
              _.flatMap {
                case (fields, rest) =>
                  field(rest, maybeMarker).map {
                    case (field, restrest) => (fields + field, restrest)
                  }
              }
            }
            .lastOption
            .flatMap {
              case Success((fields, rest)) => Some(JsonObject(fields), rest)
              case Failure(_)              => None
            }

        case ObjStart +:: obj =>
          scan(obj, Vector.empty).toOption.map {
            case (fields, rest) => JsonObject(fields.toMap) -> rest
          }
        case _ => None
      }

    // parse a single field name and value
    private def field(
        bytes: ISV,
        typ: Option[Byte] = None
    ): Try[((String, JsonValue), ISV)] =
      bytes.prepended(Markers.String) match {
        case bString(name, value) =>
          parse(typ.map(value.prepended).getOrElse(value)).map {
            case (jsonValue, rest) => (name.value, jsonValue) -> rest
          }
        case _ => Failure(new JsonException("Object missing field name"))
      }

    // parse the fields in an object until the end marker is reached
    @tailrec
    private def scan(
        bs: ISV,
        fields: Seq[(String, JsonValue)]
    ): Try[(Seq[(String, JsonValue)], ISV)] =
      bs match {
        case ObjEnd +:: rest => Success(fields, rest)
        case NoOp +:: rest   => scan(rest, fields)
        case bs =>
          field(bs) match {
            case Success((field, rest)) => scan(rest, fields :+ field)
            case Failure(ex)            => Failure(ex)
          }
      }
  }

  object +:: {
    def unapply(bytes: ISV): Option[(Byte, ISV)] =
      bytes.headOption.map(_ -> bytes.drop(1))
  }
}

trait LowConflictMarkers {
  val Null: Byte     = 'Z'
  val NoOp: Byte     = 'N'
  val True: Byte     = 'T'
  val False: Byte    = 'F'
  val Int8: Byte     = 'i'
  val UInt8: Byte    = 'U'
  val Int16: Byte    = 'I'
  val Int32: Byte    = 'l'
  val Int64: Byte    = 'L'
  val Float32: Byte  = 'd'
  val Float64: Byte  = 'D'
  val BigNum: Byte   = 'H'
  val ArrStart: Byte = '['
  val ArrEnd: Byte   = ']'
  val ObjStart: Byte = '{'
  val ObjEnd: Byte   = '}'
  val Count: Byte    = '#'
  val Type: Byte     = '$'
}
object Markers extends LowConflictMarkers {
  val Char: Byte   = 'C'
  val String: Byte = 'S'
}
