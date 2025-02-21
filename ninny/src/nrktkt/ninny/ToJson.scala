package nrktkt.ninny

import nrktkt.ninny.ast.JsonValue

trait ToJsonValue[A, +Json <: JsonValue] {
  def to(a: A): Option[Json]

  def postprocess[J <: JsonValue](
      f: PartialFunction[Option[Json], J]
  ): ToJsonValue[A, J] =
    a => f.lift(to(a))

  def postprocess[J <: JsonValue](
      f: Option[Json] => J
  ): ToSomeJsonValue[A, J] =
    a => f(to(a))

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
  override def contramap[B](f: B => A): ToSomeJsonValue[B, Json] = b =>
    toSome(f(b))

  def postprocess[J <: JsonValue](
      f: Json => J
  )(implicit i: DummyImplicit): ToSomeJsonValue[A, J] =
    a => f(toSome(a))

  def postprocess[J <: JsonValue](
      f: Json => Option[J]
  ): ToJsonValue[A, J] =
    a => f(toSome(a))
}
