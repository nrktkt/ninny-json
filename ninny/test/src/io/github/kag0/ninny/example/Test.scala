package io.github.kag0.ninny.example

import java.util.NoSuchElementException

import io.github.kag0.ninny._
import io.github.kag0.ninny.ast._

import scala.util.{Failure, Try}

object Test extends App {
  val sampleValues = obj(
    "string" -> """¯\_(ツ)_/¯""",
    "number" -> 1.79e308,
    "bool"   -> true,
    "false"  -> false,
    "null"   -> null,
    "unit"   -> ()
  )

  val sampleArray =
    arr(sampleValues, "yup", 123d, false, Seq(sampleValues: JsonValue))

  val sampleObject =
    sampleValues ++ obj("object" -> sampleValues, "array" -> sampleArray)

  val jsonString = sampleObject.toString
  println(jsonString)

  val parsed = Json.parse(jsonString).get
  println(parsed == sampleObject)
  println(parsed.array.to[Seq[JsonValue]].get == sampleArray.values)
}
