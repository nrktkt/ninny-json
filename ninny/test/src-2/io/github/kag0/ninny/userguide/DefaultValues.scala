package io.github.kag0.ninny.userguide

import io.github.kag0.ninny.FromJson
import io.github.kag0.ninny._

object DefaultValues extends App {
// format: Off

case class User(name: String, admin: Boolean = false)

implicit val userFromJson = {
  import nrktkt.ninny.FromJsonAuto.useDefaults
  FromJson.auto[User]
}

obj("name" --> "Alice").to[User]
// Success(User(Alice,false))

}
