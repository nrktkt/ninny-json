package nrktkt.ninny.compat

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import nrktkt.ninny.ast._
import nrktkt.ninny._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.OptionValues
import org.scalatest.TryValues

class CirceCompatSpec
    extends AnyFlatSpec
    with should.Matchers
    with OptionValues
    with TryValues {

  case class Example1(foo: String, bar: Seq[Int])
  object Example1 {
    implicit val decoder: Decoder[Example1] = deriveDecoder[Example1]
    implicit val encoder: Encoder[Example1] = deriveEncoder[Example1]
  }

  case class Example2(foo: String, bar: Seq[Int])
  object Example2 {
    implicit val toJson   = ToJson.auto[Example2]
    implicit val fromJson = FromJson.auto[Example2]
  }

  val ex1 = Example1("baz", Seq(1, 2, 3))
  val ex2 = Example2("baz", Seq(1, 2, 3))

  val ex1json = nrktkt.ninny.obj("foo" ~> "baz", "bar" ~> Seq(1, 2, 3))
  val ex2json = io.circe.Json.obj(
    "foo" -> io.circe.Json.fromString("baz"),
    "bar" -> io.circe.Json.fromValues(Seq(1, 2, 3).map(io.circe.Json.fromInt))
  )

  "Circe typeclasses" should "write ninny json" in {
    import CirceToNinny._

    val json = ex1.toSomeJson
    json shouldEqual ex1json
  }

  it should "read ninny json" in {
    import CirceToNinny._

    val objekt = ex1json.to[Example1].success.value
    objekt shouldEqual ex1
  }

  "ninny typeclasses" should "write circe json" in {
    import NinnyToCirce._
    val json = ex2.asJson
    json shouldEqual ex2json
  }

  it should "read circe json" in {
    import NinnyToCirce._

    val objekt = ex2json.as[Example2].toOption
    objekt shouldEqual Some(ex2)
  }
}
