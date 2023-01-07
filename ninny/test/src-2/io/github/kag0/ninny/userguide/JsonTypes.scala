package io.github.kag0.ninny.userguide

import nrktkt.ninny.ast.JsonDecimal
import nrktkt.ninny._

object JsonTypes {
// format: off

case class Money(value: BigDecimal)

val moneyToJson: ToSomeJsonValue[Money, JsonDecimal] = 
  ToJson(money => JsonDecimal(money.value))

/*

val moneyToJson: ToSomeJsonValue[Money, JsonDecimal] = 
  ToJson(money => JsonDouble(money.value.toDouble))
                            ^
type mismatch;
  found   : nrktkt.ninny.ast.JsonDouble
  required: nrktkt.ninny.ast.JsonDecimal

*/
}
