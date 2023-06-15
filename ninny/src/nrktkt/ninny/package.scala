package nrktkt

import nrktkt.ninny.ast._
import nrktkt.ninny.magnetic.{SomeJsonMagnet}
import scala.language.dynamics
import scala.util.Try

package object ninny {

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
    def -->[A: ToJson](a: A): (String, Option[JsonValue]) = s -> a.toJson
    // I couldn't decide on syntax, so I'll throw both out there
    def ~>[A: ToJson](a: A): (String, Option[JsonValue]) = s -> a.toJson
  }

  type ToJson[A]           = ToJsonValue[A, JsonValue]
  type ToJsonObject[A]     = ToJsonValue[A, JsonObject]
  type ToSomeJson[A]       = ToSomeJsonValue[A, JsonValue]
  type ToSomeJsonObject[A] = ToSomeJsonValue[A, JsonObject]

  // Import nrktkt.ninny.magnetic.obj if the magnet pattern is preferrable
  def obj(fields: (String, Option[JsonValue])*): JsonObject =
    JsonObject(
      fields.collect { case (name, Some(json)) =>
        name -> json
      }.toMap
    )

  // @deprecated(
  //  message =
  //    "Use nrktkt.ninny.magnetic.arr instead, this may be replaced with a magnet-free signature in the future"
  // )
  def arr(values: SomeJsonMagnet*) = magnetic.arr(values: _*)
}
