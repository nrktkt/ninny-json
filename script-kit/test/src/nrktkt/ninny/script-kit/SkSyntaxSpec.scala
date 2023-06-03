package nrktkt.ninny.scriptkit

import nrktkt.ninny._
import nrktkt.ninny.ast._
import nrktkt.ninny.scriptkit._
import scala.util.Try
import org.scalatest.flatspec._
import org.scalatest.matchers._
import org.scalatest.TryValues
import org.scalatest.OptionValues

class SkSyntaxSpec extends AnyFlatSpec with should.Matchers {

  val o = obj("obj" ~> obj("present" ~> 5))

  "lookup finalization syntax" should "return present values" in {
    o.obj.present.! shouldEqual JsonNumber(5)
  }

  it should "throw on absent values" in {
    assertThrows[NoSuchElementException] {
      o.obj.absent.!
    }
  }

  it should "return option for absent values" in {
    o.obj.absent.? shouldEqual None
    o.obj.present.? shouldEqual Some(JsonNumber(5))
  }
}
