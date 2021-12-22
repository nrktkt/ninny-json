package io.github.kag0.ninny

import java.time.temporal.ChronoUnit
import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import java.util.NoSuchElementException

import io.github.kag0.ninny.ast._
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._

import scala.util.{Success, Try}
import java.util.UUID
import scala.util.Random
import scala.collection.compat.immutable.ArraySeq
import java.util.Base64
import io.github.kag0.ninny.magnetic.JsonMagnet
import io.github.kag0.ninny.magnetic.SomeJsonMagnet
import scala.annotation.Annotation

class JsonSpec
    extends AnyFlatSpec
    with should.Matchers
    with TryValues
    with OptionValues {

  it should "work" in {
    val sampleValues = obj(
      "string" -> """¯\_(ツ)_/¯""",
      "number" -> 1.79e308,
      "bool"   -> true,
      "false"  -> false,
      "null"   -> JsonNull,
      "unit"   -> ((): Unit),
      "some"   -> "str"
    )

    val sampleArray =
      arr(sampleValues, "yup", 123d, false, Seq(sampleValues: JsonValue))

    val sampleObject =
      JsonObject(
        sampleValues.values ++ obj(
          "object" -> sampleValues,
          "array"  -> sampleArray
        ).values
      )

    val jsonString = sampleObject.toString
    println(jsonString)

    val parsed = Json.parse(jsonString).get
    println(parsed == sampleObject)
    println(parsed.array.to[Seq[JsonValue]].get == sampleArray.values)

    5.toSomeJson[JsonNumber] shouldEqual JsonNumber(5d)
    5.toJson shouldEqual Some(JsonNumber(5d))
  }

  case class SampleValues(
      string: String,
      number: Double,
      bool: Boolean,
      `false`: Boolean, // semi auto derivation won't work with false literal type
      `null`: Null,
      unit: Unit,
      none: Option[String],
      some: Option[String]
  )

  object SampleValues {

    implicit val toSomeJson: ToSomeJsonObject[SampleValues] =
      ToJson.auto[SampleValues]

    implicit val fromJson: FromJson[SampleValues] = FromJson.fromSome(json =>
      for {
        string <- json.string.to[String]
        number <- json.number.to[Double]
        bool   <- json.bool.to[Boolean]
        f <-
          json.`false`
            .to[Boolean]
            .flatMap[Boolean](if (_) fail() else Success(false))
        n    <- json.`null`.to[Null]
        unit <- json.unit.to[Unit]
        none <- json.none.to[Option[String]]
        some <- json.some.to[Option[String]]
      } yield SampleValues(string, number, bool, f, n, unit, none, some)
    )
  }

  val sampleValuesAst = obj(
    "string" -> """¯\_(ツ)_/¯""",
    "number" -> 1.79e308,
    "bool"   -> true,
    "false"  -> false,
    "null"   -> JsonNull,
    "unit"   -> ((): Unit),
    "some"   -> Some("str")
  )

  val sampleValuesObj = SampleValues(
    """¯\_(ツ)_/¯""",
    1.79e308,
    true,
    false,
    null,
    (),
    None,
    Some("str")
  )

  val sampleValuesString =
    """
        |{
        |  "string": "\u00AF\\_(\u30C4)_\/\u00AF",
        |  "number": 1.79e308,
        |  "bool": true,
        |  "false": false,
        |  "null": null,
        |  "unit": [],
        |  "some": "str"
        |}
        |""".stripMargin

  "Primitives" should "parse in and out of JSON strings" in {
    val sampleValuesAstParsed = Json.parse(sampleValuesString)
    sampleValuesAstParsed.success.value shouldEqual sampleValuesAst

    val sampleValuesAstGenerated = sampleValuesObj.toSomeJson
    sampleValuesAstGenerated shouldEqual sampleValuesAst
    sampleValuesAstGenerated shouldEqual sampleValuesAstParsed.success.value

    val sampleValuesObjGenerated = sampleValuesAstParsed.to[SampleValues]
    sampleValuesObjGenerated.success.value shouldEqual sampleValuesObj
  }

  val exampleObjectString =
    """
    |{
    |  "Image": {
    |    "Width":  800,
    |    "Height": 600,
    |    "Title":  "View from 15th Floor",
    |    "Thumbnail": {
    |    "Url":    "http://www.example.com/image/481989943",
    |    "Height": 125,
    |    "Width":  100
    |  },
    |    "Animated" : false,
    |    "IDs": [116, 943, 234, 38793]
    |  }
    |}
    |""".stripMargin

  val exampleArrayString =
    """
      |[
      |  {
      |    "precision": "zip",
      |    "Latitude":  37.7668,
      |    "Longitude": -122.3959,
      |    "Address":   "",
      |    "City":      "SAN FRANCISCO",
      |    "State":     "CA",
      |    "Zip":       "94107",
      |    "Country":   "US"
      |  },
      |  {
      |    "precision": "zip",
      |    "Latitude":  37.371991,
      |    "Longitude": -122.026020,
      |    "Address":   "",
      |    "City":      "SUNNYVALE",
      |    "State":     "CA",
      |    "Zip":       "94085",
      |    "Country":   "US"
      |  }
      |]
      |""".stripMargin

  val exampleObjectAst = obj(
    "Image" -> obj(
      "Width"  -> 800,
      "Height" -> 600,
      "Title"  -> "View from 15th Floor",
      "Thumbnail" -> obj(
        "Url"    -> "http://www.example.com/image/481989943",
        "Height" -> 125,
        "Width"  -> 100
      ),
      "Animated" -> false,
      "IDs"      -> arr(116, 943, 234, 38793)
    )
  )

  val exampleArrayAst = arr(
    obj(
      "precision" -> "zip",
      "Latitude"  -> 37.7668,
      "Longitude" -> -122.3959,
      "Address"   -> "",
      "City"      -> "SAN FRANCISCO",
      "State"     -> "CA",
      "Zip"       -> "94107",
      "Country"   -> "US"
    ),
    obj(
      "precision" -> "zip",
      "Latitude"  -> 37.371991,
      "Longitude" -> -122.026020,
      "Address"   -> "",
      "City"      -> "SUNNYVALE",
      "State"     -> "CA",
      "Zip"       -> "94085",
      "Country"   -> "US"
    )
  )

  case class Image(
      Width: Int,
      Height: Int,
      Title: Option[String],
      Thumbnail: Option[Image],
      Url: Option[String],
      Animated: Option[Boolean],
      IDs: Seq[Int]
  )

  object Image {
    implicit val fromJson: FromJson[Image] = FromJson.fromSome(json =>
      for {
        w        <- json.Width.to[Int]
        h        <- json.Height.to[Int]
        title    <- json.Title.to[Option[String]]
        thumb    <- json.Thumbnail.to[Option[Image]]
        url      <- json.Url.to[Option[String]]
        animated <- json.Animated.to[Option[Boolean]]
        ids      <- json.IDs.to[Option[Seq[Int]]].map(_.getOrElse(Nil))
      } yield Image(w, h, title, thumb, url, animated, ids)
    )

    implicit val toJson: ToSomeJson[Image] = a =>
      obj(
        "Width"     -> a.Width,
        "Height"    -> a.Height,
        "Title"     -> a.Title,
        "Thumbnail" -> a.Thumbnail,
        "Url"       -> a.Url,
        "Animated"  -> a.Animated,
        "IDs"       -> (if (a.IDs.isEmpty) None else a.IDs)
      )
  }

  object Precision extends Enumeration {
    type Precision = Value
    val zip, house = Value
  }

  implicit val precisionFromJson: FromJson[Precision.Value] =
    FromJson.fromSome(_.to[String].flatMap(p => Try(Precision.withName(p))))
  implicit val precisionToJson: ToSomeJson[Precision.Value] = a =>
    JsonString(a.toString)

  case class Address(
      precision: Precision.Value,
      Latitude: Double,
      Longitude: Double,
      Address: String,
      City: String,
      State: String,
      Zip: String,
      Country: String
  )

  object Address {
    implicit val fromJson: FromJson[Address] = FromJson.auto[Address]

    implicit val toJson: ToSomeJson[Address] = a =>
      obj(
        "precision" -> a.precision,
        "Latitude"  -> a.Latitude,
        "Longitude" -> a.Longitude,
        "Address"   -> a.Address,
        "City"      -> a.City,
        "State"     -> a.State,
        "Zip"       -> a.Zip,
        "Country"   -> a.Country
      )
  }

  val exampleObject =
    Image(
      800,
      600,
      Title = Some("View from 15th Floor"),
      Thumbnail = Some(
        Image(
          100,
          125,
          None,
          Thumbnail = None,
          Some("http://www.example.com/image/481989943"),
          None,
          Nil
        )
      ),
      Url = None,
      Animated = Some(false),
      IDs = Seq(116, 943, 234, 38793)
    )

  val exampleArray =
    Seq(
      Address(
        Precision.zip,
        37.7668,
        -122.3959,
        "",
        "SAN FRANCISCO",
        "CA",
        "94107",
        "US"
      ),
      Address(
        Precision.zip,
        37.371991,
        -122.026020,
        "",
        "SUNNYVALE",
        "CA",
        "94085",
        "US"
      )
    )

  "The giant test case" should "pass" in {
    // from json

    // // obj

    val hopefullyExampleObjectAstParsed = Json.parse(exampleObjectString)
    val exampleObjectAstParsed = hopefullyExampleObjectAstParsed.success.value

    exampleObjectAstParsed shouldEqual exampleObjectAst

    val exampleObjectFromJson =
      hopefullyExampleObjectAstParsed.*.Image.to[Image]

    exampleObjectFromJson.success.value shouldEqual exampleObject

    // // arr

    val array = Array(1, 2, 3, 4)
    array.toSomeJson shouldEqual arr(1, 2, 3, 4)
    Json.parse("[1, 2, 3, 4]").to[Array[Int]].success.value shouldEqual array

    val hopefullyExampleArrayAstParsed = Json.parse(exampleArrayString)
    val exampleArrayAstParsed = hopefullyExampleArrayAstParsed.success.value

    exampleArrayAstParsed shouldEqual exampleArrayAst

    val exampleArrayFromJson = hopefullyExampleArrayAstParsed.*.to[Seq[Address]]

    exampleArrayFromJson.success.value shouldEqual exampleArray

    // // values

    Json.parse(""""Hello world!"""").to[String] shouldEqual Success(
      "Hello world!"
    )
    Json.parse("42").to[Int] shouldEqual Success(42)
    Json.parse("6.022E23").to[Double] shouldEqual Success(6.022e23)
    Json.parse("true").to[Boolean] shouldEqual Success(true)

    // to json

    // // obj

    val exampleObjectAstGenerated = Map("Image" -> exampleObject).toSomeJson
    exampleObjectAstGenerated shouldEqual exampleObjectAst

    val exampleObjectStringRendered = Json.render(exampleObjectAstGenerated)

    Json
      .parse(exampleObjectStringRendered)
      .success
      .value shouldEqual exampleObjectAst

    // // arr

    val exampleArrayAstGenerated = exampleArray.toSomeJson
    exampleArrayAstGenerated shouldEqual exampleArrayAst

    val exampleArrayStringRendered = Json.render(exampleArrayAstGenerated)

    Json
      .parse(exampleArrayStringRendered)
      .success
      .value shouldEqual exampleArrayAst

    // // values

    Json.render("Hello world!".toSomeJson) shouldEqual """"Hello world!""""
    Json.render(42.toSomeJson) shouldEqual "42"
    Json.render(6.022e23.toSomeJson) shouldEqual "6.022E23"
    Json.render(true.toSomeJson) shouldEqual "true"
    Json.render(JsonNull) shouldEqual "null"
    Json.render(
      JsonString("""¯\_("ツ)_/¯""")
    ) shouldEqual """"¯\\_(\"ツ)_/¯""""
    Json.render(JsonString("\u0000")) shouldEqual "\"\\u0000\""

    // misc

    exampleArrayAstParsed(0).precision.to[String] shouldBe Success("zip")
    exampleArrayAstParsed(2).maybeJson shouldBe None

    (exampleObjectAstParsed / "Image" / "Width").*.NotAField.maybeJson shouldBe None

    (obj() / "field")
      .to[Int]
      .failed
      .get
      .getCause shouldBe a[NoSuchElementException]
    exampleObjectAst.Image.to[String].failed.get shouldBe a[JsonException]
    exampleObjectAst.Image.to[Boolean].failed.get shouldBe a[JsonException]
    exampleObjectAst.Image.to[Null].failed.get shouldBe a[JsonException]
    exampleObjectAst.Image.to[Double].failed.get shouldBe a[JsonException]

    JsonNull.to[Unit].success.value
    ().toSomeJson shouldEqual JsonArray(Nil)

    JsonNull.to[Seq[String]].failed.get shouldBe a[JsonException]

    JsonNull.to[Option[String]] shouldEqual Success(None)

    Success(
      JsonNull
    ).*.noFieldsHere.butWeCanGetCodeCoverage.maybeHopefullyJson shouldEqual Success(
      None
    )

    Success(
      JsonNull
    ).*.coverage.forThe(100).maybeHopefullyJson shouldEqual Success(
      None
    )

    Some("string").toSomeJson shouldEqual JsonString("string")
    Some(Option("string")).toJson shouldEqual Some(JsonString("string"))
  }

  "FromJson" should "preprocess values" in {
    val intFromStringFromJson = FromJsonInstances.intFromJson.preprocess {
      case Some(JsonString(s)) => JsonNumber(s.toDouble)
    }
    intFromStringFromJson.from(JsonString("5")).success.value shouldBe 5
  }

  "JsonObjects" should "rename fields" in {
    val json    = obj("foo" -> "bar")
    val renamed = json.renameField("foo", "baz")
    (renamed / "baz").value shouldEqual JsonString("bar")
    (renamed / "foo") shouldEqual None
  }

  "JsonArrays" should "return none on indexes out of bounds" in {
    val array = Some(arr("one", "two", 3))

    array(1).maybeJson shouldBe defined
    array(-1).maybeJson should not be defined
    array(3).maybeJson should not be defined

    val tryArray = Success(arr("one", "two", 3))

    tryArray(1).maybeHopefullyJson.success.value shouldBe defined
    tryArray(-1).maybeHopefullyJson.success.value shouldBe None
    tryArray(3).maybeHopefullyJson.success.value shouldBe None
  }

  it should "prepend correctly" in {
    val tail = arr(4, 5, 6)

    3 +: tail shouldEqual arr(3, 4, 5, 6)
  }

  it should "append correctly" in {
    val head = arr(1, 2, 3)

    head :+ 4 shouldEqual arr(1, 2, 3, 4)
  }

  it should "precat correctly" in {
    val head = arr(1, 2, 3)
    val tail = arr(4, 5, 6)

    head.values ++: tail shouldEqual arr(1, 2, 3, 4, 5, 6)

    head ++: tail shouldEqual arr(1, 2, 3, 4, 5, 6)
  }

  it should "concat correctly" in {
    val head = arr(1, 2, 3)
    val tail = arr(4, 5, 6)

    head :++ tail.values shouldEqual arr(1, 2, 3, 4, 5, 6)

    head :++ tail shouldEqual arr(1, 2, 3, 4, 5, 6)
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

    long.toSomeJson shouldEqual JsonDouble(5)
    int.toSomeJson shouldEqual JsonDouble(5)
    byte.toSomeJson shouldEqual JsonDouble(5)
    double.toSomeJson shouldEqual JsonDouble(double)
    float.toSomeJson shouldEqual JsonDouble(5.5)
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

  "JsonBlob" should "encode and decode to base64" in {
    val bytes = new Array[Byte](16)
    Random.nextBytes(bytes)
    val byteSeq = ArraySeq.unsafeWrapArray(bytes)
    val byteString = '"' + Base64.getUrlEncoder.withoutPadding
      .encodeToString(bytes) + '"'

    Json.render(JsonBlob(byteSeq)) shouldEqual byteString

    Json
      .parse(
        Json.render( // render so that it's parsed back as a JsonString
          byteSeq.toSomeJson
        )
      )
      .to[ArraySeq[Byte]]
      .success
      .value shouldEqual byteSeq
  }

  "Java 8 time" should "work" in {
    val now = ZonedDateTime.now()

    now shouldEqual now.toSomeJson.to[ZonedDateTime].success.value

    now.toOffsetDateTime shouldEqual now.toOffsetDateTime.toSomeJson
      .to[OffsetDateTime]
      .success
      .value

    now.toInstant.truncatedTo(
      ChronoUnit.SECONDS
    ) shouldEqual now.toInstant.toSomeJson
      .to[Instant]
      .success
      .value
  }

  "UUIDs" should "work" in {
    val id = UUID.randomUUID

    val json = id.toSomeJson

    val parsed = json.to[UUID].success

    parsed.value shouldEqual id
  }

  it should "not throw exceptions" in {
    JsonString("asdfajos;o;ohsa").to[UUID].isSuccess shouldBe false
  }

  "magnet free syntax" should "work" in {
    val o = obj(
      "foo" -> 5,
      "bar" --> 7,
      "baz" ~> 11
    )

    o.bar.maybeJson.value shouldEqual JsonNumber(7)
    o.baz.maybeJson.value shouldEqual JsonNumber(11)
  }

  it should "give useful compiler errors" in {
    trait T
    implicit def t1: ToJson[T] = ???
    implicit def t2: ToJson[T] = ???

    assertDoesNotCompile(""" obj("t" ~> new T {}) """)
  }

  "magnets" should "automatically wrap/unwrap" in {
    val mag: SomeJsonMagnet          = JsonString("foo")
    val json: JsonValue              = mag
    val maybeMag: JsonMagnet         = json
    val maybeJson: Option[JsonValue] = maybeMag

    maybeJson.value shouldEqual JsonString("foo")
    json shouldEqual mag
    maybeJson shouldEqual maybeMag
  }
  Annotation

  "larger than 22 field classes" should "work" in {
    case class Big(
        a: Int,
        b: Int,
        c: Int,
        d: Int,
        e: Int,
        f: Int,
        g: Int,
        h: Int,
        i: Int,
        j: Int,
        k: Int,
        l: Int,
        m: Int,
        n: Int,
        o: Int,
        p: Int,
        q: Int,
        r: Int,
        s: Int,
        t: Int,
        u: Int,
        v: Int,
        w: Int,
        x: Int,
        y: Int,
        z: Int
    )
    implicit val json = ToAndFromJson.auto[Big]
    val big = Big(1, 2, 2, 1234, 1234, 124, 1234, 1234, 1234, 1234, 123, 2134,
      1324, 1234, 1234, 1, 234, 1234, 14, 23, 132, 13, 431, 31, 3412, 1432)
    Json
      .parse(Json.render(big.toSomeJson))
      .to[Big]
      .success
      .value shouldEqual big
  }
}
