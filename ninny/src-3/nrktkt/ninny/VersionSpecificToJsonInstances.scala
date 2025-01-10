package nrktkt.ninny

import nrktkt.ninny.ast.JsonObject
import nrktkt.ninny.NullPointerBehavior
import nrktkt.ninny.NullPointerBehavior.Handle

trait VersionSpecificToJsonInstances {
  implicit def recordToJson[V, G <: String, Tail <: Tuple](implicit
    valueToJson: ToJson[V],
    tailToJson: ToSomeJsonObject[Tail],
    nullPointerBehavior: NullPointerBehavior
  ): ToSomeJsonObject[(G, V) *: Tail] = {
    ToJson { case (name, value) *: tail =>
      val tailJson = tailToJson.toSome(tail)
      nullPointerBehavior match {
        case Handle(behavior) if value == null =>
          tailJson + (name -> behavior())
        case _ =>
          val maybeValueJson = valueToJson.to(value)
          maybeValueJson match {
            case Some(valueJson) => tailJson + (name -> valueJson)
            case None            => tailJson
          }
      }
    }
  }

  implicit val hNilToJson: ToSomeJsonObject[EmptyTuple] = _ => JsonObject(Map.empty)
}
