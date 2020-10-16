package io.github.kag0.ninny.compat

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.TryValues
import org.scalatest.OptionValues
import play.api.libs.json.{Reads, Json => PlayJson}
import io.github.kag0.ninny.{Json => NinnyJson, _}
import PlayCompat._

class PlayCompatSpec
    extends AnyFlatSpec
    with Matchers
    with TryValues
    with OptionValues {

  case class Example1(foo: String, bar: Seq[Int])
  object Example1 {
    implicit val format = PlayJson.format[Example1]
  }

  case class Example2(foo: String, bar: Seq[Int])
  object Example2 {
    implicit val toJson   = ToJson.auto[Example2]
    implicit val fromJson = FromJson.auto[Example2]
  }

  val ex1 = Example1("baz", Seq(1, 2, 3))
  val ex2 = Example2("baz", Seq(1, 2, 3))
  val ex1json = obj("foo" -> "baz", "bar" -> Seq(1, 2, 3))
  val ex2json = PlayJson.obj("foo" -> "baz", "bar" -> PlayJson.arr(1, 2, 3))

  "Play typeclasses" should "write ninny json" in {
    val json = ex1.toSomeJson
    json shouldEqual ex1json
  }

  it should "read ninny json" in {
    val objekt = ex1json.to[Example1].success.value
    objekt shouldEqual ex1
  }

  "ninny typeclasses" should "write play json" in {
    val json = PlayJson.toJson(ex2)
    json shouldEqual ex2json
  }

  it should "read play json" in {
    val objekt = ex2json.as[Example2]
    objekt shouldEqual ex2
  }
}
