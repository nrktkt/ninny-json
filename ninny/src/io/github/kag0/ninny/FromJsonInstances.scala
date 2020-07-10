package io.github.kag0.ninny

import io.github.kag0.ninny.ast._

import scala.util.{Failure, Success, Try}

trait FromJsonInstances {
  implicit val stringFromJson: FromJson[String] = FromJson.fromSome {
    case JsonString(value) => Success(value)
    case json              => Failure(new Exception(s"Expected string, got $json"))
  }

  implicit val doubleFromJson: FromJson[Double] = FromJson.fromSome {
    case JsonNumber(value) => Success(value)
    case json              => Failure(new Exception(s"Expected number, got $json"))
  }

  implicit val booleanFromJson: FromJson[Boolean] = FromJson.fromSome {
    case JsonBoolean(value) => Success(value)
    case json               => Failure(new Exception(s"Expected boolean, got $json"))
  }

  implicit val nullFromJson: FromJson[Null] = FromJson.fromSome {
    case JsonNull => Success(null)
    case json     => Failure(new Exception(s"Expected null, got $json"))
  }

  implicit def optionFromJson[A: FromJson]: FromJson[Option[A]] = {
    case Some(JsonNull) => Success(None)
    case Some(json)     => FromJson[A].from(json).map(Some(_))
    case None           => Success(None)
  }
}
