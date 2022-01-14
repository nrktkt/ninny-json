package nrktkt.ninny

import nrktkt.ninny.ast.JsonObject
import shapeless.{::, HList, HNil, Lazy}

trait VersionSpecificToJsonInstances {

  implicit def recordToJson[V, Tail <: HList](implicit
      valueToJson: Lazy[ToJson[V]],
      tailToJson: ToSomeJsonObject[Tail]
  ): ToSomeJsonObject[(String, V) :: Tail] = {
    ToJson { case (name, value) :: tail =>
      val maybeValueJson = valueToJson.value.to(value)
      val tailJson       = tailToJson.toSome(tail)

      maybeValueJson match {
        case Some(valueJson) => tailJson + (name -> valueJson)
        case None            => tailJson
      }
    }
  }

  implicit val hNilToJson: ToSomeJsonObject[HNil] = _ => JsonObject(Map.empty)
}
