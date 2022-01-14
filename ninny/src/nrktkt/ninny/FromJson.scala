package nrktkt.ninny

import java.util.NoSuchElementException

import nrktkt.ninny.ast.JsonValue

import scala.util.{Failure, Try}

trait FromJson[A] {
  def from(maybeJson: Option[JsonValue]): Try[A]
  def from(json: JsonValue): Try[A] = from(Some(json))

  def map[B](f: A => B): FromJson[B] = from(_).map(f)

  def preprocess(
      f: PartialFunction[Option[JsonValue], JsonValue]
  ): FromJson[A] = json => from(f.lift(json).orElse(json))
}

object FromJson extends FromJsonInstances with ProductFromJson {
  def apply[A: FromJson]: FromJson[A] = implicitly
  def fromSome[A](read: JsonValue => Try[A]): FromJson[A] = {
    case Some(json) => read(json)
    case None =>
      Failure(
        new JsonException(
          "Tried to read a mandatory field that was absent",
          new NoSuchElementException("None.get")
        )
      )
  }

  def partial[A](
      read: PartialFunction[Option[JsonValue], Try[A]]
  ): FromJson[A] =
    read.applyOrElse(
      _,
      (json: Option[JsonValue]) =>
        Failure(new JsonException(json match {
          case Some(value) =>
            s"Provided JSON ($value) did not match expectations"
          case None => "Required JSON field was missing"
        }))
    )

  def auto[A: FromJsonAuto] = implicitly[FromJsonAuto[A]].fromJson
}
