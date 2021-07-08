package io.github.kag0.ninny

import scala.deriving.Mirror
import io.github.kag0.ninny.ast.JsonString
import scala.compiletime.summonAll

trait ToJsonAutoImpl {
  /*
  type Zip[A <: Tuple, B <: Tuple] = (A, B) match {
    case (a *: as, b *: bs) => (a, b) *: Zip[as, bs]
    case _ => EmptyTuple
  }
   */

  implicit def mirrorToJsonAuto[A <: Product](implicit
      mirror: Mirror.ProductOf[A],
      toJson: ToSomeJsonObject[
        Tuple.Zip[mirror.MirroredElemLabels, mirror.MirroredElemTypes]
      ]
  ): ToJsonAuto[A] =
    ToJsonAuto(a =>
      toJson.toSome(
        summonAll[mirror.MirroredElemLabels].zip(Tuple.fromProductTyped(a))
      )
    )

  val rec = mirrorToJsonAuto[JsonString]
}
