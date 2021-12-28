package io.github.kag0.ninny

import shapeless._
import shapeless.ops.record._
import shapeless.ops.hlist._

trait AutoToJson {
  implicit def lgToJson[
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
  ): ToSomeJson[A] = ToJsonAuto.labelledGenericToJson.toJson
}

trait AutoFromJson {
  implicit def lgFromJson[
      A,
      Record <: HList,
      Keys <: HList,
      Values <: HList,
      Annotations <: HList,
      ReplacedNames <: HList,
      Size <: Nat
  ](implicit
      labelledGeneric: LabelledGeneric.Aux[A, Record],
      fields: UnzipFields.Aux[Record, Keys, Values],
      generic: Generic.Aux[A, Values],
      annotations: Annotations.Aux[JsonName, A, Annotations],
      replaceNames: ZipWith.Aux[
        Keys,
        Annotations,
        ZipNewNames.type,
        ReplacedNames
      ],
      sizeNames: ToSized.Aux[ReplacedNames, List, String, Size],
      fromJson: Lazy[Sized[List[String], Size] => FromJson[Values]]
  ): FromJson[A] = FromJsonAuto.labelledGenericFromJson.fromJson
}

object Auto extends AutoToJson with AutoFromJson
