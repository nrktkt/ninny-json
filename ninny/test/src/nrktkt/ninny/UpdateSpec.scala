package nrktkt.ninny

import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import nrktkt.ninny.ast._

class UpdateSpec
    extends AnyFlatSpec
    with should.Matchers
    with TryValues
    with OptionValues {

  val sampleObject = obj(
    "a" ~> arr(
      obj(
        "b" ~> obj(
          "c" ~> arr(
            0,
            1,
            2,
            obj("d" ~> "foo")
          )
        )
      )
    ),
    "z" ~> "baz"
  )

  "Updating nested objects" should "work with dynamic syntax" in {
    val update = sampleObject.withUpdated.a(0).b.c(3).d

    val updated = update := "bar"
   
    updated.a(0).b.c(3).d.maybeJson.value shouldEqual JsonString("bar")

    (updated.withUpdated.a(0).b.c(3).d := "foo") shouldEqual sampleObject
  }
}
