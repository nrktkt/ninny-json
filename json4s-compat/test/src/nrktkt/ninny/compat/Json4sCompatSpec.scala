package nrktkt.ninny.compat

import nrktkt.ninny.ast._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.OptionValues
import org.scalatest.TryValues

class Json4sCompatSpec
    extends AnyFlatSpec
    with should.Matchers
    with OptionValues
    with TryValues {
  {
    import org.json4s.JsonAST._
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
        "array"       -> Seq(JsonNull)
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
        "array"       -> Seq(JsonNull)
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

  "Json4s DSL" should "build objects" in {
    // from example on json4s website
    import Json4sCompat._
    case class Winner(id: Long, numbers: List[Int])
    case class Lotto(
        id: Long,
        winningNumbers: List[Int],
        winners: List[Winner],
        drawDate: Option[java.util.Date]
    )
    val winners = List(
      Winner(23, List(2, 45, 34, 23, 3, 5)),
      Winner(54, List(52, 3, 12, 11, 18, 22))
    )
    val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)
    val json: JsonObject =
      ("lotto" ->
        ("lotto-id"        -> lotto.id) ~
        ("winning-numbers" -> lotto.winningNumbers) ~
        ("draw-date"       -> lotto.drawDate.map(_.toString)) ~
        ("winners" ->
          lotto.winners.map { w =>
            (("winner-id" -> w.id) ~
              ("numbers"  -> w.numbers))
          })) ~ ("hmm, don't love that this is needed" -> None)
    json.lotto.`lotto-id`.to[Long].success.value shouldEqual lotto.id
    json.lotto.winners(0).numbers.to[Seq[Int]].success.value shouldEqual lotto
      .winners(0)
      .numbers
  }
}
