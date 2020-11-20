package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue
import org.typelevel.jawn.Parser
import io.github.kag0.ninny.jawn._

object Json {
  def parse(s: String, highPrecision: Boolean = false) = {
    implicit def facade = if (highPrecision) DecimalFacade else DoubleFacade
    Parser.parseFromString(s)
  }
  
  def render(json: JsonValue) = json.toString
}
