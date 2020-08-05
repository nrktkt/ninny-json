package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue
import org.typelevel.jawn.Parser
import io.github.kag0.ninny.jawn.Facade

object Json {
  def parse(s: String)        = Parser.parseFromString(s)
  def render(json: JsonValue) = json.toString
}
