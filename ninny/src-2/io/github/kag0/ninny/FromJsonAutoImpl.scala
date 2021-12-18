package io.github.kag0.ninny

import shapeless.LabelledGeneric
import shapeless.Lazy

trait FromJsonAutoImpl {
  implicit def labelledGenericFromJson[A, Head](implicit
      generic: LabelledGeneric.Aux[A, Head],
      headFromJson: Lazy[FromJson[Head]]
  ) = new FromJsonAuto[A](headFromJson.value.from(_).map(generic.from))
}
