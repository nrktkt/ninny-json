package io.github.kag0.ninny.binary

import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import scala.collection.immutable.ArraySeq
import io.github.kag0.ninny.ast._
import io.github.kag0.ninny._
import java.nio.ByteBuffer

class UbJsonSpec extends AnyFlatSpec with should.Matchers with TryValues {
  "Value types" should "parse null" in {
    val binary = ArraySeq.unsafeWrapArray(Array[Byte]('Z'))
    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual JsonNull
  }

  it should "render null" in {
    UbJson.render(JsonNull) shouldEqual ArraySeq[Byte]('Z')
  }

  it should "render characters" in {
    UbJson.render(JsonString("!")) shouldEqual ArraySeq[Byte]('C', '!')
  }

  it should "not render non-ascii as characters" in {
    UbJson.render(JsonString("😀")) shouldEqual
      ArraySeq[Byte](
        'S',
        'i',
        4,
        0xf0.toByte,
        0x9f.toByte,
        0x98.toByte,
        0x80.toByte
      )
  }

  it should "parse unsigned bytes" in {
    val binary = ArraySeq[Byte]('U', -1)
    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual JsonDouble(0xff)

    val binary2 = ArraySeq[Byte]('U', -6)
    val parsed2 = UbJson.parse(binary2).success.value
    parsed2 shouldEqual JsonDouble(250)
  }

  it should "render unsigned bytes" in {
    UbJson.render(JsonDouble(255)) shouldEqual ArraySeq[Byte]('U', -1)
    UbJson.render(JsonDouble(250)) shouldEqual ArraySeq[Byte]('U', -6)
  }

  it should "parse signed bytes" in {
    val binary = ArraySeq[Byte]('i', 0)
    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual JsonDouble(0)

    UbJson.parse(ArraySeq[Byte]('i', -1)).success.value shouldEqual
      JsonDouble(-1)

    UbJson.parse(ArraySeq[Byte]('i', -128)).success.value shouldEqual
      JsonDouble(-128)

    UbJson.parse(ArraySeq[Byte]('i', 127)).success.value shouldEqual
      JsonDouble(127)
  }

  it should "render signed bytes" in {
    UbJson.render(JsonDouble(0)) shouldEqual ArraySeq[Byte]('i', 0)
    UbJson.render(JsonDouble(-1)) shouldEqual ArraySeq[Byte]('i', -1)
    UbJson.render(JsonDouble(Byte.MinValue)) shouldEqual
      ArraySeq[Byte]('i', Byte.MinValue)
    UbJson.render(JsonDouble(Byte.MaxValue)) shouldEqual
      ArraySeq[Byte]('i', Byte.MaxValue)
  }

  it should "parse shorts" in {
    UbJson.parse(ArraySeq[Byte]('I', -1, -1)).success.value shouldEqual
      JsonDouble(-1)
    UbJson
      .parse(ArraySeq[Byte]('I', 0x80.toByte, 0x00))
      .success
      .value shouldEqual
      JsonDouble(Short.MinValue)

    UbJson
      .parse(ArraySeq[Byte]('I', 0x7f, -1))
      .success
      .value shouldEqual
      JsonDouble(Short.MaxValue)
  }

  it should "render shorts" in {
    UbJson.render(JsonDouble(Short.MinValue)) shouldEqual
      ArraySeq[Byte]('I', 0x80.toByte, 0)
    UbJson.render(JsonDouble(Short.MaxValue)) shouldEqual
      ArraySeq[Byte]('I', 0x7f, -1)
  }

  it should "parse ints" in {
    UbJson.parse(ArraySeq[Byte]('l', -1, -1, -1, -1)).success.value shouldEqual
      JsonDouble(-1)

    UbJson
      .parse(ArraySeq[Byte]('l', 0x80.toByte, 0, 0, 0))
      .success
      .value shouldEqual
      JsonDouble(Int.MinValue)

    UbJson
      .parse(ArraySeq[Byte]('l', 0x7f, -1, -1, -1))
      .success
      .value shouldEqual
      JsonDouble(Int.MaxValue)
  }

  it should "render ints" in {
    UbJson.render(JsonDouble(Int.MinValue)) shouldEqual
      ArraySeq[Byte]('l', 0x80.toByte, 0, 0, 0)
    UbJson.render(JsonDouble(Int.MaxValue)) shouldEqual
      ArraySeq[Byte]('l', 0x7f, -1, -1, -1)
  }

  it should "parse longs" in {
    UbJson
      .parse(ArraySeq[Byte]('L', -1, -1, -1, -1, -1, -1, -1, -1))
      .success
      .value shouldEqual
      JsonDouble(-1)

    UbJson
      .parse(ArraySeq[Byte]('L', 0x80.toByte, 0, 0, 0, 0, 0, 0, 0))
      .success
      .value shouldEqual
      JsonDouble(Long.MinValue)

    UbJson
      .parse(ArraySeq[Byte]('L', 0x7f, -1, -1, -1, -1, -1, -1, -1))
      .success
      .value shouldEqual
      JsonDouble(Long.MaxValue)
  }

  it should "render longs" in {
    UbJson.render(JsonDouble(Long.MinValue)) shouldEqual
      ArraySeq[Byte]('L', 0x80.toByte, 0, 0, 0, 0, 0, 0, 0)
    UbJson.render(JsonDouble(Long.MaxValue)) shouldEqual
      ArraySeq[Byte]('L', 0x7f, -1, -1, -1, -1, -1, -1, -1)
  }

  it should "parse floats" in {
    val float = 12.34f
    val binary =
      ArraySeq[Byte]('d') :++ ByteBuffer.allocate(4).putFloat(float).array()
    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual JsonDouble(float)

    UbJson
      .parse(
        ArraySeq[Byte]('d') :++ ByteBuffer
          .allocate(4)
          .putFloat(Float.MaxValue)
          .array
      )
      .success
      .value shouldEqual JsonDouble(Float.MaxValue)

    UbJson
      .parse(
        ArraySeq[Byte]('d') :++ ByteBuffer
          .allocate(4)
          .putFloat(Float.MinValue)
          .array
      )
      .success
      .value shouldEqual JsonDouble(Float.MinValue)
  }

  it should "render floats" in {
    UbJson.render(JsonDouble(12.34f)) shouldEqual
      ArraySeq[Byte]('d') :++ ByteBuffer
        .allocate(4)
        .putFloat(12.34f)
        .array

    UbJson.render(JsonDouble(Float.MinValue)) shouldEqual
      ArraySeq[Byte]('d') :++ ByteBuffer
        .allocate(4)
        .putFloat(Float.MinValue)
        .array

    UbJson.render(JsonDouble(Float.MaxValue)) shouldEqual
      ArraySeq[Byte]('d') :++ ByteBuffer
        .allocate(4)
        .putFloat(Float.MaxValue)
        .array
  }

  it should "parse doubles" in {
    UbJson
      .parse(
        ArraySeq[Byte]('D') :++ ByteBuffer
          .allocate(8)
          .putDouble(-1)
          .array
      )
      .success
      .value shouldEqual JsonDouble(-1)

    UbJson
      .parse(
        ArraySeq[Byte]('D') :++ ByteBuffer
          .allocate(8)
          .putDouble(Double.MaxValue)
          .array
      )
      .success
      .value shouldEqual JsonDouble(Double.MaxValue)

    UbJson
      .parse(
        ArraySeq[Byte]('D') :++ ByteBuffer
          .allocate(8)
          .putDouble(Double.MinValue)
          .array
      )
      .success
      .value shouldEqual JsonDouble(Double.MinValue)
  }

  it should "render doubles" in {
    UbJson.render(JsonDouble(12.34)) shouldEqual
      ArraySeq[Byte]('D') :++ ByteBuffer
        .allocate(8)
        .putDouble(12.34)
        .array

    UbJson.render(JsonDouble(Double.MinValue)) shouldEqual
      ArraySeq[Byte]('D') :++ ByteBuffer
        .allocate(8)
        .putDouble(Double.MinValue)
        .array

    UbJson.render(JsonDouble(Double.MaxValue)) shouldEqual
      ArraySeq[Byte]('D') :++ ByteBuffer
        .allocate(8)
        .putDouble(Double.MaxValue)
        .array
  }

  it should "parse high precision numbers" in {
    val binary = ArraySeq[Byte]('H', 'U', 10, '1', '2', '3', '4', '5', '.', '6',
      '7', '8', '9')
    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual BigDecimal("12345.6789")

    UbJson
      .parse(ArraySeq[Byte]('H', 'U', 6, '3', '.', '7', 'e', '-', '5'))
      .success
      .value shouldEqual BigDecimal("3.7E-5")
  }

  it should "render high precision numbers" in {
    UbJson.render(JsonDecimal(BigDecimal("12345.6789"))) shouldEqual
      ArraySeq[Byte]('H', 'i', 10, '1', '2', '3', '4', '5', '.', '6', '7', '8',
        '9')

    UbJson.render(JsonDecimal(BigDecimal("3.7e-5"))) shouldEqual
      ArraySeq[Byte]('H', 'i', 6, '3', '.', '7', 'E', '-', '5')
  }

  val stringBin            = ArraySeq[Byte]('S', 'i', 3) :++ "foo".getBytes
  val float                = 12.34f
  def floatBytes(n: Float) = ByteBuffer.allocate(4).putFloat(n).array
  def floatBin(n: Float) =
    ArraySeq[Byte]('d') :++ floatBytes(n)

  def byte(b: Byte): Byte = b

  val nullField: ArraySeq[Byte] =
    ArraySeq[Byte]('i', 4) :++ "null".getBytes :+ byte('Z')
  val stringField: ArraySeq[Byte] =
    ArraySeq[Byte]('i', 6) :++ "string".getBytes :++ stringBin
  val boolField: ArraySeq[Byte] =
    ArraySeq[Byte]('i', 4) :++ "bool".getBytes :+ byte('T')
  def numberField(name: String, n: Float, typ: Boolean = true): ArraySeq[Byte] =
    ArraySeq[Byte]('i', name.size.toByte) :++ name.getBytes :++
      (if (typ) floatBin(n) else floatBytes(n))
  val charField = ArraySeq[Byte]('i', 4, 'c', 'h', 'a', 'r', 'C', '!')

  "Object containers" should "parse simple objects" in {

    val binary: ArraySeq[Byte] = ArraySeq[Byte]('{') :++
      nullField :++
      stringField :+
      byte('N') :++
      boolField :++
      numberField("num", float) :++
      charField :+
      '}'

    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual obj(
      "null"   -> JsonNull,
      "string" -> "foo",
      "bool"   -> true,
      "num"    -> float,
      "char"   -> "!"
    )
  }

  it should "parse sized objects" in {
    val binary: ArraySeq[Byte] = ArraySeq[Byte]('{', '#', 'U', 5) :++
      nullField :++
      stringField :++
      boolField :++
      numberField("num", float) :++
      charField

    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual obj(
      "null"   -> JsonNull,
      "string" -> "foo",
      "bool"   -> true,
      "num"    -> float,
      "char"   -> "!"
    )
  }

  it should "parse typed objects" in {
    val (a, b, c, d) = (1.2f, 2.3f, 3.4f, 4.5f)
    val binary: ArraySeq[Byte] = ArraySeq[Byte]('{', '$', 'd', '#', 'U', 4) :++
      numberField("a", a, false) :++
      numberField("b", b, false) :++
      numberField("c", c, false) :++
      numberField("d", d, false)

    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual obj(
      "a" -> a,
      "b" -> b,
      "c" -> c,
      "d" -> d
    )
  }

  it should "render objects" in {
    UbJson.render(
      obj(
        "null"   -> JsonNull,
        "string" -> "foo",
        "bool"   -> true,
        "num"    -> float,
        "char"   -> "!"
      )
    ) shouldEqual ArraySeq[Byte]('{') :++
      stringField :++
      numberField("num", float) :++
      boolField :++
      charField :++
      nullField :+
      byte('}')
  }

  "Array containers" should "parse simple arrays" in {
    val binary: ArraySeq[Byte] =
      ArraySeq[Byte]('[') :++
        stringBin :++
        floatBin(float) :+
        byte('N') :+
        byte('Z') :+
        byte('F') :+
        ']'

    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual arr("foo", float, JsonNull, false)
  }

  it should "parse sized arrays" in {
    val binary: ArraySeq[Byte] =
      ArraySeq[Byte]('[', '#', 'U', 4) :++
        stringBin :++
        floatBin(float) :+
        byte('Z') :+
        byte('F')

    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual arr("foo", float, JsonNull, false)
  }

  it should "parse typed arrays" in {
    val binary: ArraySeq[Byte] =
      ArraySeq[Byte]('[', '$', 'd', '#', 'U', 4) :++
        floatBytes(1.2f) :++
        floatBytes(2.3f) :++
        floatBytes(3.4f) :++
        floatBytes(4.5f)

    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual arr(1.2f, 2.3f, 3.4f, 4.5f)
  }

  it should "render arrays" in {
    UbJson.render(arr("foo", float, JsonNull, false)) shouldEqual
      ArraySeq[Byte]('[') :++
        stringBin :++
        floatBin(float) :+
        byte('Z') :+
        byte('F') :+
        ']'
  }

  it should "parse blobs" in {
    val binary: ArraySeq[Byte] =
      ArraySeq[Byte]('[', '$', 'U', '#', 'U', 3, 1, 2, 3)
    val parsed = UbJson.parse(binary).success.value
    parsed shouldEqual JsonBlob(ArraySeq[Byte](1, 2, 3))
  }

  it should "render blobs" in {
    val binary = ArraySeq[Byte](1, 2, 3, 4, 5, 0, 9, 8, 7, 6)
    UbJson.render(JsonBlob(binary)) shouldEqual
      ArraySeq[Byte]('[', '$', 'U', '#', 'i', 10).concat(binary)
  }

  "UBJSON" should "satisfy identity" in {
    val o = obj(
      "foo"    -> "bar",
      "num"    -> 5,
      "bool"   -> true,
      "bignum" -> BigDecimal("12.3e7"),
      "float"  -> 12.3,
      "null"   -> JsonNull,
      "bin"    -> JsonBlob(ArraySeq[Byte](1, 2, 3)),
      "arr" -> arr(
        1,
        true,
        12.3,
        obj(
          "nested" -> "!",
          "again!" -> arr(1, 2, 3)
        )
      )
    )

    UbJson.parse(UbJson.render(o)).success.value shouldEqual o
  }
}
