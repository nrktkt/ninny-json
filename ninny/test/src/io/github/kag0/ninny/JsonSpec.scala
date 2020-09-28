package io.github.kag0.ninny

import java.time.temporal.ChronoUnit
import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import java.util.NoSuchElementException

import io.github.kag0.ninny.ast._
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import scala.collection.immutable._

import scala.util.{Success, Try}

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
      "unit"   -> (),
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

    implicit val toSomeJson = ToJson.auto[SampleValues]

    implicit val fromJson = FromJson.fromSome(json =>
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
    "unit"   -> (),
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

  implicit val precisionFromJson =
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
    implicit val fromJson = FromJson.auto[Address]

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

    //// obj

    val hopefullyExampleObjectAstParsed = Json.parse(exampleObjectString)
    val exampleObjectAstParsed          = hopefullyExampleObjectAstParsed.success.value

    exampleObjectAstParsed shouldEqual exampleObjectAst

    val exampleObjectFromJson =
      hopefullyExampleObjectAstParsed.*.Image.to[Image]

    exampleObjectFromJson.success.value shouldEqual exampleObject

    //// arr

    val hopefullyExampleArrayAstParsed = Json.parse(exampleArrayString)
    val exampleArrayAstParsed          = hopefullyExampleArrayAstParsed.success.value

    exampleArrayAstParsed shouldEqual exampleArrayAst

    val exampleArrayFromJson = hopefullyExampleArrayAstParsed.*.to[Seq[Address]]

    exampleArrayFromJson.success.value shouldEqual exampleArray

    //// values

    Json.parse(""""Hello world!"""").to[String] shouldEqual Success(
      "Hello world!"
    )
    Json.parse("42").to[Int] shouldEqual Success(42)
    Json.parse("6.022E23").to[Double] shouldEqual Success(6.022e23)
    Json.parse("true").to[Boolean] shouldEqual Success(true)

    // to json

    //// obj

    val exampleObjectAstGenerated = Map("Image" -> exampleObject).toSomeJson
    exampleObjectAstGenerated shouldEqual exampleObjectAst

    val exampleObjectStringRendered = Json.render(exampleObjectAstGenerated)

    Json
      .parse(exampleObjectStringRendered)
      .success
      .value shouldEqual exampleObjectAst

    //// arr

    val exampleArrayAstGenerated = exampleArray.toSomeJson
    exampleArrayAstGenerated shouldEqual exampleArrayAst

    val exampleArrayStringRendered = Json.render(exampleArrayAstGenerated)

    Json
      .parse(exampleArrayStringRendered)
      .success
      .value shouldEqual exampleArrayAst

    //// values

    Json.render("Hello world!".toSomeJson) shouldEqual """"Hello world!""""
    Json.render(42.toSomeJson) shouldEqual "42"
    Json.render(6.022e23.toSomeJson) shouldEqual "6.022E23"
    Json.render(true.toSomeJson) shouldEqual "true"
    Json.render(JsonNull) shouldEqual "null"
    Json.render(
      JsonString("""¯\_(ツ)_/¯""")
    ) shouldEqual "\"\\u00af\\\\_(\\u30c4)_\\/\\u00af\""

    // misc

    exampleArrayAstParsed(0).precision.to[String] shouldBe Success("zip")
    exampleArrayAstParsed(2).maybeJson shouldBe None

    (exampleObjectAstParsed / "Image" / "Width").*.NotAField.maybeJson shouldBe None

    (obj() / "field").to[Int].failed.get shouldBe a[NoSuchElementException]
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
    val head = arr(1, 2, 3)
    val tail = arr(4, 5, 6)

    head.values ++: tail shouldEqual arr(1, 2, 3, 4, 5, 6)

    head ++: tail shouldEqual arr(1, 2, 3, 4, 5, 6)
  }

  it should "append correctly" in {
    val head = arr(1, 2, 3)
    val tail = arr(4, 5, 6)

    head :++ tail.values shouldEqual arr(1, 2, 3, 4, 5, 6)

    head :++ tail shouldEqual arr(1, 2, 3, 4, 5, 6)
  }

  "numbers" should "fail on precision loss" in {
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
    JsonNumber(Double.MaxValue)
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
    JsonNumber(Long.MaxValue)
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
}
