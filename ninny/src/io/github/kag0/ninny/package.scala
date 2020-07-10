package io.github.kag0

import io.github.kag0.ninny.ast.{JsonArray, JsonObject, JsonValue}

import scala.util.Try

package object ninny extends ToJsonInstances with FromJsonInstances {

  def fromJson[A: FromJson](json: JsonValue): Try[A] = FromJson[A].from(json)

  def fromJson[A: FromJson](json: Option[JsonValue]): Try[A] =
    FromJson[A].from(json)

  def toJson[A: ToJson](a: A)         = ToJson[A].to(a)
  def toSomeJson[A: ToSomeJson](a: A) = ToSomeJson[A].toSome(a)

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

    implicit def from[A: ToJson](a: A) =
      new JsonMagnet {
        val json = ToJson[A].to(a)
      }

    implicit def fromJson(js: JsonValue) =
      new JsonMagnet {
        val json = Some(js)
      }
  }
}
