package nrktkt.ninny.compat

import nrktkt.ninny.ast.JsonValue

import nrktkt.ninny._
import nrktkt.ninny.ast.JsonObject
import scala.util.Try
import nrktkt.ninny.ast.JsonArray
import nrktkt.ninny.ast.JsonString
import nrktkt.ninny.ast.JsonDouble
import nrktkt.ninny.ast.JsonBoolean

object Json4sCompat {
  implicit class TildeTupleSyntax[A](val pair: (String, A)) extends AnyVal {
    def ~[B: ToJson](newPair: (String, B))(implicit
        aToJson: ToJson[A]
    ): JsonObject =
      obj(pair._1 -> pair._2, newPair._1 -> newPair._2)
  }

  implicit class JsonObjectSyntax(json: JsonObject) extends {
    def ~[B: ToJson](pair: (String, B)): JsonObject = json + pair
    def ~(other: JsonObject): JsonObject            = json ++ other
  }

  implicit class Json4sSyntax(val json: JsonValue) extends AnyVal {
    def \(name: String)                    = (json / name).get
    def extract[A: FromJson]: A            = json.to[A].get
    def extractOpt[A: FromJson]: Option[A] = json.to[Option[A]].get
  }

  implicit class Json4sMaybeSyntax(val json: Option[JsonValue]) extends AnyVal {
    def \(name: String)                    = (json.flatMap(_ / name)).get
    def extract[A: FromJson]: A            = json.to[A].get
    def extractOpt[A: FromJson]: Option[A] = json.to[Option[A]].get
  }

  object ast {
    type JValue  = JsonValue
    type JObject = JsonObject
    val JObject = JsonObject
    type JArray = JsonArray
    val JArray = JsonArray
    type JString = JsonString
    val JString = JsonString
    type JBool = JsonBoolean
    val JBool = JsonBoolean
    type JDouble = JsonDouble
    val JDouble = JsonDouble
    type JInt = JsonDouble
    val JInt = JsonDouble
  }
}
