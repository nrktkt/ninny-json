package io.github.kag0.ninny

import shapeless.LabelledGeneric
import shapeless.Lazy

trait ToJsonAutoImpl {

  implicit def labelledGenericToJson[A, Head](implicit
      generic: LabelledGeneric.Aux[A, Head],
      headToJson: Lazy[ToSomeJsonObject[Head]]
  ) = new ToJsonAuto[A](a => headToJson.value.toSome(generic.to(a)))
}
