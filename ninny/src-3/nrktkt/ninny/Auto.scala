package nrktkt.ninny

import scala.deriving.Mirror

trait AutoToJson {
  implicit inline def lgToJson[
    A <: Product,
    OverridingNames <: Tuple](implicit 
      mirror: Mirror.ProductOf[A],
      names: Annotation.Aux[A, OverridingNames]): ToSomeJson[A] =
        ToJsonAuto.labelledGenericToJson.toJson
}

trait AutoFromJson {
  implicit inline def lgFromJson[
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
  ): FromJson[A] = FromJsonAuto.labelledGenericFromJson.fromJson
}

object Auto extends AutoToJson with AutoFromJson {
  private[ninny] type OverrideNames[Names <: Tuple, Overriden <: Tuple] <: Tuple =
    (Names, Overriden) match
      case (aHead *: aTail, Null *: bTail) => aHead *: OverrideNames[aTail, bTail]
      case (aHead *: aTail, str *: bTail) => str *: OverrideNames[aTail, bTail]
      case (EmptyTuple, _) => EmptyTuple

  private[ninny] type WrappedSize[T <: Tuple] = T match
    case a *: EmptyTuple => Added[Zero]
    case a *: b => Added[WrappedSize[b]]

}
