package nrktkt.ninny

import nrktkt.ninny.ast._
import scala.language.dynamics

case class Update(json: JsonValue, path: Seq[Either[String, Int]])
    extends Dynamic {
  def :=[A: ToSomeJson](replaceWith: A): JsonValue =
    (json, path) match {
      // end of the path, replace json with the new value
      case (_, Seq()) => replaceWith.toSomeJson

      // replace the key in an object
      case (JsonObject(values), Left(key) +: tail) if values.contains(key) =>
        val newValue = Update(values(key), tail) := replaceWith
        JsonObject(values.updated(key, newValue))

      // replace the value at an index in an array
      case (JsonArray(values), Right(i) +: tail) if values.length > i =>
        val newValue = Update(values(i), tail) := replaceWith
        JsonArray(values.updated(i, newValue))

      // the update is trying to replace a location which doesn't exist
      case _ => json
    }

  def selectDynamic(name: String)        = Update(json, path :+ Left(name))
  def apply(i: Int)                      = Update(json, path :+ Right(i))
  def applyDynamic(name: String)(i: Int) = selectDynamic(name)(i)
}
