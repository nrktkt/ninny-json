package nrktkt.ninny

import scala.annotation.nowarn
import scala.deriving.Mirror
import nrktkt.ninny.DefaultOptions._
import scala.compiletime.ops.int
import scala.compiletime.ops.int._

trait FromJsonAutoImpl {

  implicit def useDefaults[A, O <: Tuple](implicit
    defaults: DefaultOptions.Aux[A, O]
  ): Defaults.Aux[A, O] =
    new Defaults[A] {
      type Out = O
      def apply() = defaults()
    }

  implicit inline def labelledGenericFromJson[
      A <: Product,
      Values <: Tuple,
      Defaults <: Tuple,
      OverridingNames <: Tuple,
      Size <: Numlike
  ](implicit
      mirror: Mirror.ProductOf[A],
      ev: Size =:= Auto.WrappedSize[mirror.MirroredElemTypes],
      defaults: Defaults.Aux[A, Defaults],
      annotation: Annotation.Aux[A, OverridingNames],
      from: (Sized[List[String], Size], Defaults) => FromJson[Values]
  ): FromJsonAuto[A] = {
    val names = Sized[A, OverridingNames, Size](mirror)
    val fromJson = from(names, defaults()).map(mirror.fromProduct(_))
    new FromJsonAuto[A](fromJson)
  }

}

trait Defaults[A] {
  type Out <: Tuple
  def apply(): Out
}

object Defaults {
  type Aux[A, O <: Tuple] = Defaults[A] { type Out = O }

  implicit def ignoreDefaults[A, O <: Tuple](implicit
      defaults: DefaultOptions.Aux[A, O],
  ): Defaults.Aux[A, O] =
    new Defaults[A] {
      type Out = O
      def apply() = 
        def makeEmptyTuple(tuple: Tuple): Tuple =
          tuple match
            case head *: tail => None *: makeEmptyTuple(tail)
            case EmptyTuple => EmptyTuple
        makeEmptyTuple(defaults()).asInstanceOf[Out]
    }
}
