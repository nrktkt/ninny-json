package nrktkt.ninny

import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import nrktkt.ninny.ast._

class ToJsonInstancesSpec extends AnyFlatSpec with should.Matchers {
  "Either values" should
    "convert right to json" in {
      val right                       = Right[Int, String]("test")
      val either: Either[Int, String] = right
      val expected                    = Some(JsonString("test"))
      right.toJson shouldEqual expected
      either.toJson shouldEqual expected
    }

  it should "convert left to json" in {
    val left                        = Left[Int, String](5)
    val either: Either[Int, String] = left
    val expected                    = Some(JsonNumber(5))
    left.toJson shouldEqual expected
    either.toJson shouldEqual expected
  }

  it should "provide ToSomeJson when available" in {
    {
      val left                        = Left[Int, String](5)
      val either: Either[Int, String] = left
      val expected                    = JsonNumber(5)
      left.toSomeJson shouldEqual expected
      either.toSomeJson shouldEqual expected
    }
    {
      val right                       = Right[Int, String]("test")
      val either: Either[Int, String] = right
      val expected                    = JsonString("test")
      right.toSomeJson shouldEqual expected
      either.toSomeJson shouldEqual expected
    }
  }
}
