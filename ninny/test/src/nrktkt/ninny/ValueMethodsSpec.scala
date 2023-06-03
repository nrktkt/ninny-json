package nrktkt.ninny

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers._
import org.scalatest.OptionValues
import org.scalatest.TryValues

class ValueMethodsSpec
    extends AnyFlatSpec
    with should.Matchers
    with OptionValues
    with TryValues {

  "JsonObject deep merge" should "merge object fields" in {
    val left   = obj("inner" ~> obj("field" ~> "value"))
    val right  = obj("inner" ~> obj("field2" ~> "value2"))
    val merged = left +++ right
    merged.inner.field.to[String].success.value shouldEqual "value"
    merged.inner.field2.to[String].success.value shouldEqual "value2"
  }

  it should "overwrite with the new value" in {
    val left =
      obj(
        "inner" ~> obj(
          "field"  ~> "value",
          "field2" ~> obj("x" ~> "y"),
          "field3" ~> 2
        )
      )
    val right = obj(
      "inner" ~> obj(
        "field"  ~> "value2",
        "field2" ~> 1,
        "field3" ~> obj("x" ~> "y")
      )
    )
    val merged = left +++ right
    merged.inner.field.to[String].success.value shouldEqual "value2"
    merged.inner.field2.to[Int].success.value shouldEqual 1
    merged.inner.field3.x.to[String].success.value shouldEqual "y"
  }
}
