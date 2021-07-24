package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue

trait ToJsonValue[A, +Json <: JsonValue] {
  def to(a: A): Option[Json]

  def contramap[B](f: B => A): ToJsonValue[B, Json] = b => to(f(b))
}

object ToJsonValue extends ToJsonInstances

object ToJson extends ProductToJson {

  def apply[A: ToJson]: ToJson[A] = implicitly[ToJson[A]]

  def apply[A, Json <: JsonValue](fn: A => Option[Json]): ToJsonValue[A, Json] =
    (a: A) => fn(a)

  def apply[A, Json <: JsonValue](fn: A => Json): ToSomeJsonValue[A, Json] =
    (a: A) => fn(a)

  def auto[A: ToJsonAuto] = implicitly[ToJsonAuto[A]].toJson
}

trait ToSomeJsonValue[A, +Json <: JsonValue] extends ToJsonValue[A, Json] {
  def toSome(a: A): Json
  override def to(a: A) = Some(toSome(a))
}
