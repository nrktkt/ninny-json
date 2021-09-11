package io.github.kag0

import io.github.kag0.ninny.ast._

import scala.collection.immutable._
import scala.language.{dynamics, implicitConversions}
import scala.util.Try

package object ninny {

  type ToJson[A]           = ToJsonValue[A, JsonValue]
  type ToJsonObject[A]     = ToJsonValue[A, JsonObject]
  type ToSomeJson[A]       = ToSomeJsonValue[A, JsonValue]
  type ToSomeJsonObject[A] = ToSomeJsonValue[A, JsonObject]

  trait ToAndFromJson[A] extends ToJson[A] with FromJson[A]
  object ToAndFromJson extends ProductToAndFromJson {

    implicit def apply[A: ToJson: FromJson] =
      new ToAndFromJson[A] {
        def to(a: A)                           = a.toJson
        def from(maybeJson: Option[JsonValue]) = maybeJson.to[A]
      }

    def auto[A: ToJsonAuto: FromJsonAuto]: ToAndFromJson[A] = {
      implicit val toJson   = implicitly[ToJsonAuto[A]].toJson
      implicit val fromJson = implicitly[FromJsonAuto[A]].fromJson
      ToAndFromJson[A]
    }
  }

  def obj(nameValues: (String, JsonMagnet)*): JsonObject =
    JsonObject(
      nameValues.toMap.collect {
        case (name, JsonMagnet(json)) => name -> json
      }
    )

  def arr(values: JsonMagnet*) =
    JsonArray(Seq(values.flatMap(_.json): _*))

  trait JsonMagnet {
    def json: Option[JsonValue]
  }

  object JsonMagnet {
    def unapply(arg: JsonMagnet): Option[JsonValue] = arg.json

    implicit def fromA[A: ToJson](a: A) =
      new JsonMagnet {
        val json = if (a == null) Some(JsonNull) else ToJson[A].to(a)
      }

    implicit def fromJson(js: JsonValue) =
      new JsonMagnet {
        val json = Some(js)
      }
  }

  trait SomeJsonMagnet extends JsonMagnet {
    def someJson: JsonValue
    def json = Some(someJson)
  }

  object SomeJsonMagnet {
    implicit def fromA[A: ToSomeJson](a: A) =
      new SomeJsonMagnet {
        val someJson = if (a == null) JsonNull else a.toSomeJson
      }

    implicit def fromJson(js: JsonValue) =
      new SomeJsonMagnet {
        val someJson = js
      }
  }

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
}
