package io.github.kag0.ninny

import io.github.kag0.ninny.ast._

trait ToJsonInstances {
  implicit def stringToJson: ToSomeJson[String]   = JsonString(_)
  implicit def booleanToJson: ToSomeJson[Boolean] = JsonBoolean(_)
  implicit def nullToJson: ToSomeJson[Null]       = _ => JsonNull
  implicit def longToJson: ToSomeJson[Double]     = JsonNumber(_)
  //implicit def jsonToJson: ToSomeJson[JsonValue] = js => js

  implicit def optionToJson[A: ToJson]: ToJson[Option[A]] =
    a => a.flatMap(ToJson[A].to(_))
}
