package nrktkt.ninny

import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import io.github.kag0.ninny.ast._

class ToJsonInstancesSpec extends AnyFlatSpec with should.Matchers {
  "Either values" should 
    "convert right to json" in {
      val right = Right[Int, String]("test")
      val either: Either[Int, String] = right
      val expected = Some(JsonString("test"))
      right.toJson shouldEqual expected
      either.toJson shouldEqual expected
    }

    it should "convert left to json" in {
      val left = Left[Int, String](5)
      val either: Either[Int, String] = left
      val expected = Some(JsonNumber(5))
      left.toJson shouldEqual expected
      either.toJson shouldEqual expected
    }
}
