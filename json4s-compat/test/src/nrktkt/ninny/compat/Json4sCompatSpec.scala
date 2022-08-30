package nrktkt.ninny.compat

import org.json4s._

import nrktkt.ninny.ast._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.OptionValues

class Json4sCompatSpec
    extends AnyFlatSpec
    with should.Matchers
    with OptionValues {

  "Conversion from json4s to ninny" should "return None on JNothing" in {
    Json4sCompat.toNinnyJson(JNothing) shouldEqual None
  }

  it should "convert all types" in {
    val json4s = JObject(
      "absent"      -> JNothing,
      "long"        -> JLong(1),
      "bigInt"      -> JInt(1),
      "exactNumber" -> JDecimal(BigDecimal("1.23")),
      "bool"        -> JBool(true),
      "string"      -> JString("value"),
      "null"        -> JNull,
      "array"       -> JArray(List(JNull))
    )

    val ninny = nrktkt.ninny.obj(
      "long"        -> JsonDouble(1),
      "bigInt"      -> BigDecimal(1),
      "exactNumber" -> BigDecimal("1.23"),
      "bool"        -> true,
      "string"      -> "value",
      "null"        -> JsonNull,
      "array"       -> Seq(null)
    )

    Json4sCompat.toNinnyJson(json4s).value shouldEqual ninny
  }

  "Conversion from ninny to json4s" should "convert all types" in {
    val ninny = nrktkt.ninny.obj(
      "long"        -> JsonDouble(1),
      "bigInt"      -> BigDecimal(1),
      "exactNumber" -> BigDecimal("1.23"),
      "bool"        -> true,
      "string"      -> "value",
      "null"        -> JsonNull,
      "array"       -> Seq(null)
    )

    val json4s = JObject(
      "long"        -> JDouble(1),
      "bigInt"      -> JDecimal(1),
      "exactNumber" -> JDecimal(BigDecimal("1.23")),
      "bool"        -> JBool(true),
      "string"      -> JString("value"),
      "null"        -> JNull,
      "array"       -> JArray(List(JNull))
    )

    Json4sCompat.toJson4s(ninny) shouldEqual json4s
  }
}
