package io.github.kag0

import io.github.kag0.ninny.ast.{JsonArray, JsonObject, JsonValue}
import scala.language.dynamics
import scala.util.Try

package object ninny extends ToJsonInstances with FromJsonInstances {

  type ToJson[A]           = ToJsonValue[A, JsonValue]
  type ToJsonObject[A]     = ToJsonValue[A, JsonObject]
  type ToSomeJson[A]       = ToSomeJsonValue[A, JsonValue]
  type ToSomeJsonObject[A] = ToSomeJsonValue[A, JsonObject]

  def obj(nameValues: (String, JsonMagnet)*): JsonObject =
    JsonObject(
      nameValues.toMap.collect {
        case (name, JsonMagnet(Some(json))) => name -> json
      }
    )

  def arr(values: JsonMagnet*) = JsonArray(values.flatMap(_.json))

  trait JsonMagnet {
    val json: Option[JsonValue]
  }

  object JsonMagnet {
    def unapply(arg: JsonMagnet): Option[Option[JsonValue]] = Some(arg.json)

    implicit def fromA[A: ToJson](a: A) =
      new JsonMagnet {
        val json = ToJson[A].to(a)
      }

    implicit def fromJson(js: JsonValue) =
      new JsonMagnet {
        val json = Some(js)
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

  implicit class AnySyntax[A](val a: A) extends AnyVal {
    def toJson(implicit toJson: ToJson[A])         = toJson.to(a)
    def toSomeJson(implicit toJson: ToSomeJson[A]) = toJson.toSome(a)
  }
}
