package nrktkt.ninny.compat

import nrktkt.ninny.ast.JsonValue
import nrktkt.ninny._
import nrktkt.ninny.ast._

import scala.collection.immutable
import scala.util.Try
import scala.annotation.tailrec

object Json4sCompat {
  implicit class TildeTupleSyntax[A](val pair: (String, A)) extends AnyVal {
    def ~[B: ToJson](newPair: (String, B))(implicit
        aToJson: ToJson[A]
    ): JsonObject =
      obj(pair._1 -> pair._2, newPair._1 -> newPair._2)
  }

  implicit class JsonObjectSyntax(json: JsonObject) {
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

  def toJson4s(json: JsonValue): org.json4s.JsonAST.JValue = json match {
    case JsonArray(values) =>
      org.json4s.JsonAST.JArray(values.map(toJson4s).toList)
    case blob: JsonBlob => org.json4s.JsonAST.JString(Json.render(blob))
    case JsonDecimal(preciseValue) => org.json4s.JsonAST.JDecimal(preciseValue)
    case JsonDouble(value)         => org.json4s.JsonAST.JDouble(value)
    case boolean: JsonBoolean      => org.json4s.JsonAST.JBool(boolean.value)
    case JsonNull                  => org.json4s.JsonAST.JNull
    case JsonString(value)         => org.json4s.JsonAST.JString(value)
    case JsonObject(values) =>
      org.json4s.JsonAST.JObject(values.map { case (name, value) =>
        name -> toJson4s(value)
      }.toList)
  }

  def toNinnyJson(json: org.json4s.JsonAST.JValue): Option[JsonValue] =
    json match {
      case org.json4s.JsonAST.JNothing      => None
      case org.json4s.JsonAST.JNull         => Some(JsonNull)
      case org.json4s.JsonAST.JString(s)    => Some(JsonString(s))
      case org.json4s.JsonAST.JDouble(num)  => Some(JsonDouble(num))
      case org.json4s.JsonAST.JDecimal(num) => Some(JsonDecimal(num))
      case org.json4s.JsonAST.JLong(num)    => Some(JsonDouble(num))
      case org.json4s.JsonAST.JInt(num)    => Some(JsonDecimal(BigDecimal(num)))
      case org.json4s.JsonAST.JBool(value) => Some(JsonBoolean(value))
      case org.json4s.JsonAST.JArray(arr) =>
        Some(JsonArray(arr.flatMap(toNinnyJson)))
      case org.json4s.JsonAST.JSet(set) =>
        Some(JsonArray(set.toList.flatMap(toNinnyJson _)))
      case org.json4s.JsonAST.JObject(obj) =>
        Some(JsonObject(obj.flatMap { case (name, value) =>
          toNinnyJson(value).map(name -> _)
        }.toMap))
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
