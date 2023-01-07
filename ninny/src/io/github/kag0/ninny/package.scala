package io.github.kag0

import nrktkt.ninny.ast._
import nrktkt.ninny.MaybeJsonSyntax
import scala.language.implicitConversions
import scala.util.Try
import nrktkt.ninny.HopefullyJsonSyntax
import nrktkt.ninny.HopefullyMaybeJsonSyntax
import nrktkt.ninny.AnySyntax
import nrktkt.ninny.ArrowSyntax

package object ninny
    extends VersionSpecificPackage
    with nrktkt.ninny.MagneticMethods {
  val Json = nrktkt.ninny.Json
  type ToJson[A] = nrktkt.ninny.ToJson[A]
  val ToJson = nrktkt.ninny.ToJson
  type ToJsonValue[A]      = nrktkt.ninny.ToJson[A]
  type ToSomeJson[A]       = nrktkt.ninny.ToSomeJson[A]
  type ToSomeJsonObject[A] = nrktkt.ninny.ToSomeJsonObject[A]
  type FromJson[A]         = nrktkt.ninny.FromJson[A]
  val FromJson = nrktkt.ninny.FromJson
  type ToAndFromJson[A] = nrktkt.ninny.ToAndFromJson[A]
  val ToAndFromJson = nrktkt.ninny.ToAndFromJson

  type JsonException      = nrktkt.ninny.JsonException
  type JsonFieldException = nrktkt.ninny.JsonFieldException

  val FromJsonInstances = nrktkt.ninny.FromJsonInstances

  implicit def maybeJsonSyntax(maybeJson: Option[JsonValue]): MaybeJsonSyntax =
    new MaybeJsonSyntax(maybeJson)

  implicit def hopefullyJsonSyntax(
      hopefullyJson: Try[JsonValue]
  ): HopefullyJsonSyntax =
    new HopefullyJsonSyntax(hopefullyJson)

  implicit def hopefullyMaybeJsonSyntax(
      hopefullyMaybeJson: Try[Option[JsonValue]]
  ): HopefullyMaybeJsonSyntax = new HopefullyMaybeJsonSyntax(hopefullyMaybeJson)

  implicit def anySyntax[A](a: A): AnySyntax[A]    = new AnySyntax(a)
  implicit def arrowSyntax(s: String): ArrowSyntax = new ArrowSyntax(s)
}
