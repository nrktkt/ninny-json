package io.github.kag0.ninny

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._
import scala.annotation.nowarn
import io.github.kag0.ninny.Auto.ZipNewNames

trait FromJsonAutoImpl {

  implicit def labelledGenericFromJson[
      A,
      Record <: HList,
      Keys <: HList,
      Values <: HList,
      Annotations <: HList,
      ReplacedNames <: HList,
      Size <: Nat
  ](implicit
      @nowarn lgEv: LabelledGeneric.Aux[A, Record],
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
      fromJsonForNames: Lazy[Sized[List[String], Size] => FromJson[Values]]
  ) = {
    val names    = sizeNames(replaceNames(fields.keys, annotations()))
    val fromJson = fromJsonForNames.value(names).map(generic.from)
    new FromJsonAuto[A](fromJson)
  }
}
