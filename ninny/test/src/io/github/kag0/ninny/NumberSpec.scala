package io.github.kag0.ninny

import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import io.github.kag0.ninny.ast._
class NumberSpec
    extends AnyFlatSpec
    with should.Matchers
    with TryValues
    with OptionValues {

  "floats" should "write NaN" in {
    val string = Json.render(Float.NaN.toSomeJson)
    string shouldEqual "\"NaN\""
  }

  "longs" should "convert without precision loss" in {
    Long.MaxValue.toSomeJson.to[Long].success.value shouldEqual Long.MaxValue
    Long.MinValue.toSomeJson.to[Long].success.value shouldEqual Long.MinValue
  }

  "JsonNumbers" should "fail on precision loss" in {
    JsonNumber(12.3)
      .to[Long]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonNumber(Double.MaxValue)
      .to[Long]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonDecimal(Double.MaxValue)
      .to[Long]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonNumber(Double.MinValue)
      .to[Long]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonNumber(12.3)
      .to[Int]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonNumber(Long.MaxValue)
      .to[Int]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonDecimal(Long.MaxValue)
      .to[Int]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
    JsonNumber(Long.MinValue)
      .to[Int]
      .failed
      .get
      .getCause shouldBe a[ArithmeticException]
  }

  it should "parse decimal strings and numbers" in {
    val big          = BigDecimal(Double.MaxValue) + BigDecimal("12.3")
    val parsedString = Json.parse('"' + big.toString + '"', true)
    val parsedNumber = Json.parse(s"${big.toString}", true)

    parsedNumber.to[BigDecimal].success.value shouldEqual big
    parsedString.to[BigDecimal].success.value shouldEqual big
  }

  it should "parse integer strings and numbers" in {
    val big          = BigInt(Long.MaxValue) + 1
    val parsedString = Json.parse('"' + big.toString + '"', true)
    val parsedNumber = Json.parse(s"${big.toString}", true)

    parsedString.to[BigInt].success.value shouldEqual big
    parsedNumber.to[BigInt].success.value shouldEqual big
  }

  it should "parse with high precision when requested" in {
    val parsed = Json.parse("9.88731224174273635E+308", true)

    parsed.to[Double].success.value shouldEqual Double.PositiveInfinity
    parsed
      .to[java.math.BigDecimal]
      .success
      .value shouldEqual new java.math.BigDecimal(
      "9.88731224174273635E+308"
    )
  }

  it should "write any Numeric" in {
    val long: Long     = 5
    val int: Int       = 5
    val byte: Byte     = 5
    val double: Double = 5.5
    val float: Float   = 5.5f

    long.toSomeJson shouldEqual JsonDecimal(BigDecimal(5))
    int.toSomeJson shouldEqual JsonDouble(5)
    byte.toSomeJson shouldEqual JsonDouble(5)
    double.toSomeJson shouldEqual JsonDouble(double)
    float.toSomeJson shouldEqual JsonDouble(5.5)
  }

  it should "read any primitive number" in {
    val long: Long     = 5
    val int: Int       = 5
    val byte: Byte     = 5
    val double: Double = 5.5
    val float: Float   = 5.5f
    val short: Short   = 1

    JsonDouble(long).to[Long].success.value shouldEqual long
    JsonDouble(int).to[Int].success.value shouldEqual int
    JsonDouble(byte).to[Byte].success.value shouldEqual byte
    JsonDouble(short).to[Short].success.value shouldEqual short
    JsonDouble(double).to[Double].success.value shouldEqual double
    JsonDouble(float).to[Float].success.value shouldEqual float
  }

  it should "write big numbers with high precision" in {
    val i = BigInt(123)
    val d = BigDecimal("123.456")
    i.toSomeJson shouldEqual JsonDecimal(BigDecimal(123))
    d.toSomeJson shouldEqual JsonDecimal(d)
    Json.render(
      (BigDecimal(Double.MaxValue) + Double.MaxValue).toSomeJson
    ) shouldEqual "3.5953862697246314E+308"
  }
}
