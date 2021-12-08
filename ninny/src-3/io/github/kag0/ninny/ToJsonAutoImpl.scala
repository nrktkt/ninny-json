package io.github.kag0.ninny

import scala.deriving.Mirror
import scala.deriving.Mirror.ProductOf
import io.github.kag0.ninny.ast.JsonString
import scala.compiletime.summonAll

trait ToJsonAutoImpl {

  private def values(t: Tuple): Tuple = t match
    case (h: ValueOf[_]) *: t1 => h.value *: values(t1)
    case EmptyTuple => EmptyTuple 

  implicit def mirrorToJsonAuto[A <: Product](using
      mirror: Mirror.ProductOf[A],
      toJson: ToSomeJsonObject[
        Tuple.Zip[mirror.MirroredElemLabels, mirror.MirroredElemTypes]
      ]
  ): ToJsonAuto[A] = {
    type ValueOfLabels = Tuple.Map[mirror.MirroredElemLabels, ValueOf]
    val valueOfLabels = summonAll[ValueOfLabels]
    val labels = values(valueOfLabels).asInstanceOf[mirror.MirroredElemLabels]
    
    ToJsonAuto(a =>
      toJson.toSome(
        labels.zip(Tuple.fromProductTyped(a))
      )
    )
  }

  
  given m: Mirror.ProductOf[JsonString] = summon[Mirror.ProductOf[JsonString]]
  given ToSomeJsonObject[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]] = ???
  val rec = summon[ToJsonAuto[JsonString]]
}
