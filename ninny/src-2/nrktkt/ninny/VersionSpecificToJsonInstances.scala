package nrktkt.ninny

import nrktkt.ninny.ast.JsonObject
import shapeless.{::, HList, HNil, Lazy}
import nrktkt.ninny.NullPointerBehavior.Handle

trait VersionSpecificToJsonInstances {

  implicit def recordToJson[V, Tail <: HList](implicit
      valueToJson: Lazy[ToJson[V]],
      tailToJson: ToSomeJsonObject[Tail],
      nullPointerBehavior: NullPointerBehavior
  ): ToSomeJsonObject[(String, V) :: Tail] = {
    ToJson { case (name, value) :: tail =>
      val tailJson = tailToJson.toSome(tail)
      nullPointerBehavior match {
        case Handle(behavior) if value == null =>
          tailJson + (name -> behavior())
        case _ =>
          val maybeValueJson = valueToJson.value.to(value)
          maybeValueJson match {
            case Some(valueJson) => tailJson + (name -> valueJson)
            case None            => tailJson
          }
      }
    }
  }

  implicit val hNilToJson: ToSomeJsonObject[HNil] = _ => JsonObject(Map.empty)
}
