package nrktkt.ninny

import nrktkt.ninny.ast.JsonNull
import nrktkt.ninny.ast.JsonValue

sealed trait NullPointerBehavior
object NullPointerBehavior {
  case object PassThrough extends NullPointerBehavior
  case class Handle(behavior: () => Option[JsonValue])
      extends NullPointerBehavior

  val Ignore = Handle(() => None)
  val WriteNull = Handle {
    val someNull = Some(JsonNull)
    () => someNull
  }
  implicit def default: NullPointerBehavior = PassThrough
}
