package io.github.kag0.ninny

import java.util.NoSuchElementException

import io.github.kag0.ninny.ast.JsonValue

import scala.util.{Failure, Try}

trait FromJson[A] {
  def from(maybeJson: Option[JsonValue]): Try[A]
  def from(json: JsonValue): Try[A] = from(Some(json))
}

object FromJson {
  def apply[A: FromJson]: FromJson[A] = implicitly[FromJson[A]]
  def fromSome[A](read: JsonValue => Try[A]): FromJson[A] = {
    case Some(json) => read(json)
    case None       => Failure(new NoSuchElementException())
  }
  def auto[A: FromJsonAuto] = implicitly[FromJsonAuto[A]].fromJson
}
