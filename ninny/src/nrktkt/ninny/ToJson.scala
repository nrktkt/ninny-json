package nrktkt.ninny

import nrktkt.ninny.ast.JsonValue

trait ToJson[A] {
  type Json <: JsonValue

  def to(a: A): Option[Json]
  def contramap[B](f: B => A): ToJson[B] = ToJson((b: B) => to(f(b)))
}

object ToJsonValue extends ToJsonInstances

object ToJson
    extends ToJsonInstances
    with ProductToJson
    with ToSomeJsonConstructor {

  def apply[A: ToJson]: ToJson[A] = implicitly[ToJson[A]]

  def apply[A, J <: JsonValue](
      fn: A => Option[J]
  ): ToJsonValue[A, J] = new ToJson[A] {
    type Json = J
    def to(a: A): Option[Json] = fn(a)
  }

  def auto[A: ToJsonAuto] = implicitly[ToJsonAuto[A]].toJson
}

trait ToSomeJson[A] extends ToJson[A] {
  def toSome(a: A): Json
  override def to(a: A) = Some(toSome(a))
}

object ToSomeJson extends ToSomeJsonConstructor

private[ninny] trait ToSomeJsonConstructor {
  def apply[A, J <: JsonValue](
      fn: A => J
  ): ToSomeJson[A] { type Json = J } = new ToSomeJson[A] {
    type Json = J
    def toSome(a: A): J = fn(a)
  }
}
