package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue

trait ToJsonValue[A, +Json <: JsonValue] {
  def to(a: A): Option[Json]
}

object ToJson {
  def apply[A: ToJson]: ToJson[A] = implicitly[ToJson[A]]
  def apply[A, Json <: JsonValue](fn: A => Option[Json]): ToJsonValue[A, Json] =
    (a: A) => fn(a)
  def apply[A, Json <: JsonValue](fn: A => Json): ToSomeJsonValue[A, Json] =
    (a: A) => fn(a)
}

trait ToSomeJsonValue[A, +Json <: JsonValue] extends ToJsonValue[A, Json] {
  def toSome(a: A): Json
  override def to(a: A) = Some(toSome(a))
}
