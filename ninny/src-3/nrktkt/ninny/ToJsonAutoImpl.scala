package nrktkt.ninny

import scala.deriving.Mirror
import scala.deriving.Mirror.Sum
import scala.collection.View.Zip

trait ToJsonAutoImpl {

  implicit inline def labelledGenericToJson[A <: Product, OverridingNames <: Tuple](using mirror: Mirror.ProductOf[A], annotations: Annotation.Aux[A, OverridingNames]): ToJsonAuto[A] = {
    new ToJsonAuto[A]((a: A) => {
      val keys = scala.compiletime.constValueTuple[Auto.OverrideNames[mirror.MirroredElemLabels, OverridingNames]]
      val values = Tuple.fromProductTyped(a)
      val zip = keys.zip(values)

      scala.compiletime.summonInline[ToSomeJsonObject[Tuple.Zip[Auto.OverrideNames[mirror.MirroredElemLabels, OverridingNames], mirror.MirroredElemTypes]]].toSome(zip.asInstanceOf[Tuple.Zip[Auto.OverrideNames[mirror.MirroredElemLabels, OverridingNames], mirror.MirroredElemTypes]])
    })
  }

}
