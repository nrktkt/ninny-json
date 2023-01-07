package nrktkt.ninny

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._
import nrktkt.ninny.Auto.ZipNewNames

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
  ): ToJsonAuto[A] = {
    val replacedNames = replaceNames(fields.keys, annotations())
    new ToJsonAuto[A](ToJson(a => {
      val record = generic.to(a)
      val updatedRecord = zipWithNames(
        replacedNames :: fields.values(record) :: HNil
      )
      toJson.value.toSome(updatedRecord)
    }))
  }
}
