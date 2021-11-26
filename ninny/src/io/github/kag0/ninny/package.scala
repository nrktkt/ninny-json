package io.github.kag0

import io.github.kag0.ninny.ast._
import io.github.kag0.ninny.magnetic.{JsonMagnet, SomeJsonMagnet}

import scala.language.dynamics
import scala.util.Try

package object ninny {

  type ToJson[A]           = ToJsonValue[A, JsonValue]
  type ToJsonObject[A]     = ToJsonValue[A, JsonObject]
  type ToSomeJson[A]       = ToSomeJsonValue[A, JsonValue]
  type ToSomeJsonObject[A] = ToSomeJsonValue[A, JsonObject]

  trait ToAndFromJson[A] extends ToSomeJson[A] with FromJson[A]
  object ToAndFromJson extends ProductToAndFromJson {

    implicit def apply[A: ToSomeJson: FromJson] =
      new ToAndFromJson[A] {
        def toSome(a: A)                       = a.toSomeJson
        def from(maybeJson: Option[JsonValue]) = maybeJson.to[A]
      }

    def auto[A: ToJsonAuto: FromJsonAuto]: ToAndFromJson[A] = {
      implicit val toJson   = implicitly[ToJsonAuto[A]].toJson
      implicit val fromJson = implicitly[FromJsonAuto[A]].fromJson
      ToAndFromJson[A]
    }
  }

  //@deprecated(
  //  message =
  //    "Use io.github.kag0.ninny.magnetic.obj instead, this may be replaced with a magnet-free signature in the future",
  //  since = ""
  //)
  def obj(nameValues: (String, JsonMagnet)*): JsonObject =
    magnetic.obj(nameValues: _*)

  //@deprecated(
  //  message =
  //    "Use io.github.kag0.ninny.magnetic.arr instead, this may be replaced with a magnet-free signature in the future"
  //)
  def arr(values: SomeJsonMagnet*) = magnetic.arr(values: _*)

  implicit class MaybeJsonSyntax(val maybeJson: Option[JsonValue])
      extends AnyVal
      with Dynamic {

    def to[A: FromJson] = FromJson[A].from(maybeJson)

    def selectDynamic(name: String)        = MaybeJsonSyntax(/(name))
    def apply(i: Int)                      = MaybeJsonSyntax(/(i))
    def applyDynamic(name: String)(i: Int) = selectDynamic(name)(i)

    def /(name: String) = maybeJson.flatMap(_ / name)
    def /(i: Int)       = maybeJson.flatMap(_ / i)

    def * = this
  }

  implicit class HopefullyJsonSyntax(val hopefullyJson: Try[JsonValue])
      extends AnyVal
      with Dynamic {
    def to[A: FromJson] = hopefullyJson.flatMap(_.to[A])

    def selectDynamic(name: String)        = HopefullyMaybeJsonSyntax(/(name))
    def apply(i: Int)                      = HopefullyMaybeJsonSyntax(/(i))
    def applyDynamic(name: String)(i: Int) = selectDynamic(name)(i)

    def /(name: String) = hopefullyJson.map(_ / name)
    def /(i: Int)       = hopefullyJson.map(_ / i)

    def * = this
  }

  implicit class HopefullyMaybeJsonSyntax(
      val maybeHopefullyJson: Try[Option[JsonValue]]
  ) extends AnyVal
      with Dynamic {
    def to[A: FromJson] = maybeHopefullyJson.flatMap(_.to[A])

    def selectDynamic(name: String)        = HopefullyMaybeJsonSyntax(/(name))
    def apply(i: Int)                      = HopefullyMaybeJsonSyntax(/(i))
    def applyDynamic(name: String)(i: Int) = selectDynamic(name)(i)

    def /(name: String) = maybeHopefullyJson.map(_ / name)
    def /(i: Int)       = maybeHopefullyJson.map(_ / i)

    def * = this
  }

  implicit class AnySyntax[A](val _a: A) extends AnyVal {
    def toJson[Json <: JsonValue](implicit toJson: ToJsonValue[A, Json]) =
      toJson.to(_a)

    def toSomeJson[Json <: JsonValue](implicit
        toJson: ToSomeJsonValue[A, Json]
    ) = toJson.toSome(_a)
  }

  implicit class ArrowSyntax(val s: String) extends AnyVal {
    def -->[A: ToJson](a: A) = s -> JsonMagnet(a)
    // I couldn't decide on syntax, so I'll throw both out there
    def ~>[A: ToJson](a: A) = s -> JsonMagnet(a)
  }
}
