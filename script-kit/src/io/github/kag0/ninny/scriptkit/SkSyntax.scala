package io.github.kag0.ninny.scriptkit

import scala.util.Try
import scala.util.Success
import io.github.kag0.ninny._
import io.github.kag0.ninny.ast._

class SkSyntax(value: Try[Option[JsonValue]]) {
  def ! : JsonValue         = value.get.get
  def ? : Option[JsonValue] = value.get
}
