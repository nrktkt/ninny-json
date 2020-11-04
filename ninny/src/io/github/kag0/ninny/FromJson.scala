package io.github.kag0.ninny

import java.util.NoSuchElementException

import io.github.kag0.ninny.ast.JsonValue

import scala.util.{Failure, Try}

trait FromJson[A] {
  def from(maybeJson: Option[JsonValue]): Try[A]
  def from(json: JsonValue): Try[A] = from(Some(json))

  def map[B](f: A => B): FromJson[B] = from(_).map(f)
}

object FromJson extends FromJsonInstances {
  def apply[A: FromJson]: FromJson[A] = implicitly[FromJson[A]]
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
      (_: Option[JsonValue]) =>
        Failure(new JsonException("Provided JSON did not match expectations"))
    )

  def auto[A: FromJsonAuto] = implicitly[FromJsonAuto[A]].fromJson
}
