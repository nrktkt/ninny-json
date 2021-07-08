package io.github.kag0.ninny

import _root_.io.github.kag0.ninny.ast.JsonObject

trait AdditionalToJsonInstances {
  implicit val emptyTupleToJson: ToSomeJsonObject[EmptyTuple] = _ =>
    JsonObject(Map.empty)

  implicit def recordToJson[
      Key <: String,
      Head,
      Tail <: Tuple
  ](implicit
      headToJson: ToJson[Head],
      tailToJson: ToSomeJsonObject[Tail]
  ): ToSomeJsonObject[(Key, Head) *: Tail] =
    ToJson {
      case (name, head) *: tail =>
        tail.toSomeJson + (name, head)
    }
}
