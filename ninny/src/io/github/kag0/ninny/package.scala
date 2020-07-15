package io.github.kag0

import io.github.kag0.ninny.ast.{JsonArray, JsonObject, JsonValue}

import scala.util.Try

package object ninny extends ToJsonInstances with FromJsonInstances {

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
      extends AnyVal {
    def to[A: FromJson] = FromJson[A].from(maybeJson)
  }

  implicit class HopefullyJsonSyntax(val hopefullyJson: Try[JsonValue])
      extends AnyVal {
    def to[A: FromJson] = hopefullyJson.flatMap(FromJson[A].from(_))
  }

  implicit class AnySyntax[A](val a: A) extends AnyVal {
    def toJson(implicit toJson: ToJson[A])         = toJson.to(a)
    def toSomeJson(implicit toJson: ToSomeJson[A]) = toJson.toSome(a)
  }
}
