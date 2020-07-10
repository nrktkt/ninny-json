package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue

trait ToJson[A] {
  def to(a: A): Option[JsonValue]
}

trait ToSomeJson[A] extends ToJson[A] {
  def toSome(a: A): JsonValue
  override def to(a: A) = Some(toSome(a))
}

object ToJson {
  def apply[A: ToJson]: ToJson[A] = implicitly[ToJson[A]]
  def apply[A](toJson: PartialFunction[A, JsonValue]): ToJson[A] =
    toJson.lift(_)
}

object ToSomeJson {
  def apply[A: ToSomeJson]: ToSomeJson[A] = implicitly[ToSomeJson[A]]
}
