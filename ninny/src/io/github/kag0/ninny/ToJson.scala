package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue

trait ToJson[A] {
  def to(a: A): Option[JsonValue]
}

object ToJson {
  def apply[A: ToJson]: ToJson[A] = implicitly[ToJson[A]]
}

trait ToSomeJson[A] extends ToJson[A] {
  def toSome(a: A): JsonValue
  override def to(a: A) = Some(toSome(a))
}
