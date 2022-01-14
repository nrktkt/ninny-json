package nrktkt.ninny

import nrktkt.ninny.ast.JsonValue

trait ToAndFromJson[A] extends ToSomeJson[A] with FromJson[A]
object ToAndFromJson extends ProductToAndFromJson {
  implicit def apply[A](implicit
      toJson: ToSomeJson[A],
      fromJson: FromJson[A]
  ): ToAndFromJson[A] =
    new ToAndFromJson[A] {
      def toSome(a: A)                       = toJson.toSome(a)
      def from(maybeJson: Option[JsonValue]) = fromJson.from(maybeJson)
    }

  def auto[A: ToJsonAuto: FromJsonAuto]: ToAndFromJson[A] = {
    val toJson   = implicitly[ToJsonAuto[A]].toJson
    val fromJson = implicitly[FromJsonAuto[A]].fromJson
    ToAndFromJson[A](toJson, fromJson)
  }
}
