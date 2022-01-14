package nrktkt.ninny.scriptkit

import scala.util.Try
import scala.util.Success
import nrktkt.ninny._
import nrktkt.ninny.ast._

class SkSyntax(value: Try[Option[JsonValue]]) {
  def ! : JsonValue         = value.get.get
  def ? : Option[JsonValue] = value.get
}
