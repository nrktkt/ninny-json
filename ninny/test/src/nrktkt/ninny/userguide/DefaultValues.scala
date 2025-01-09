package nrktkt.ninny.userguide

import nrktkt.ninny.FromJson
import nrktkt.ninny._

object DefaultValues extends App {
// format: Off

case class User(name: String, admin: Boolean = false)

implicit val userFromJson: FromJson[User] = {
  import nrktkt.ninny.FromJsonAuto.useDefaults
  FromJson.auto
}

obj("name" --> "Alice").to[User]
// Success(User(Alice,false))

}
