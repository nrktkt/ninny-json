package io.github.kag0.ninny

import shapeless.{LabelledGeneric, Lazy}

trait AutoToJson {
  implicit def lgToJson[A, Head](implicit
      generic: LabelledGeneric.Aux[A, Head],
      headToJson: Lazy[ToSomeJsonObject[Head]]
  ) = ToJsonAuto.labelledGenericToJson[A, Head].toJson
}

trait AutoFromJson {
  implicit def lgFromJson[A, Head](implicit
      generic: LabelledGeneric.Aux[A, Head],
      headFromJson: Lazy[FromJson[Head]]
  ) = FromJsonAuto.labelledGenericFromJson[A, Head].fromJson
}

object Auto extends AutoToJson with AutoFromJson
