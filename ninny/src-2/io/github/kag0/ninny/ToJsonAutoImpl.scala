package io.github.kag0.ninny

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._

trait ToJsonAutoImpl {
  implicit def labelledGenericToJson[
      A,
      Record <: HList,
      Keys <: HList,
      Values <: HList,
      Annotations <: HList,
      ReplacedNames <: HList,
      UpdatedRecord <: HList
  ](implicit
      generic: LabelledGeneric.Aux[A, Record],
      fields: UnzipFields.Aux[Record, Keys, Values],
      annotations: Annotations.Aux[JsonName, A, Annotations],
      replaceNames: ZipWith.Aux[
        Keys,
        Annotations,
        ZipNewNames.type,
        ReplacedNames
      ],
      zipWithNames: Zip.Aux[ReplacedNames :: Values :: HNil, UpdatedRecord],
      toJson: Lazy[ToSomeJsonObject[UpdatedRecord]]
  ): ToJsonAuto[A] =
    new ToJsonAuto[A](a => {
      val record        = generic.to(a)
      val replacedNames = replaceNames(fields.keys, annotations())
      val updatedRecord = zipWithNames(
        replacedNames :: fields.values(record) :: HNil
      )
      toJson.value.toSome(updatedRecord)
    })
}

object ZipNewNames extends Poly2 {
  implicit def newName[K <: Symbol]: Case.Aux[K, Some[JsonName], String] =
    at((_, name) => name.value.name)

  implicit def existingName[K <: Symbol]: Case.Aux[K, None.type, String] =
    at((field, _) => field.name)
}
