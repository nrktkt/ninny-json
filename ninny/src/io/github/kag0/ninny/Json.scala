package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue
import org.typelevel.jawn.Parser
import io.github.kag0.ninny.jawn._
import scala.collection.compat.immutable.ArraySeq

object Json {
  def parse(s: String, highPrecision: Boolean = false) = {
    implicit def facade = if (highPrecision) DecimalFacade else DoubleFacade
    Parser.parseFromString(s)
  }

  def parseArray(s: ArraySeq[Byte], highPrecision: Boolean = false) = {
    implicit def facade = if (highPrecision) DecimalFacade else DoubleFacade
    Parser.parseFromByteArray(s.unsafeArray.asInstanceOf[Array[Byte]])
  }

  def render(json: JsonValue) = json.toString
}
