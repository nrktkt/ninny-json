package io.github.kag0.ninny

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._
import scala.annotation.nowarn
import io.github.kag0.ninny.Auto.ZipNewNames

trait FromJsonAutoImpl {

  implicit def useDefaults[A, O <: HList](implicit
      defaults: Default.AsOptions.Aux[A, O]
  ): Defaults.Aux[A, O] =
    new Defaults[A] {
      type Out = O
      def apply() = defaults()
    }

  implicit def labelledGenericFromJson[
      A,
      Record <: HList,
      Keys <: HList,
      Values <: HList,
      Defaults <: HList,
      Annotations <: HList,
      ReplacedNames <: HList,
      Size <: Nat
  ](implicit
      @nowarn lgEv: LabelledGeneric.Aux[A, Record],
      fields: UnzipFields.Aux[Record, Keys, Values],
      defaults: Defaults.Aux[A, Defaults],
      generic: Generic.Aux[A, Values],
      annotations: Annotations.Aux[JsonName, A, Annotations],
      replaceNames: ZipWith.Aux[
        Keys,
        Annotations,
        ZipNewNames.type,
        ReplacedNames
      ],
      sizeNames: ToSized.Aux[ReplacedNames, List, String, Size],
      fromJsonForNames: Lazy[
        (Sized[List[String], Size], Defaults) => FromJson[Values]
      ]
  ) = {
    val names    = sizeNames(replaceNames(fields.keys, annotations()))
    val fromJson = fromJsonForNames.value(names, defaults()).map(generic.from)
    new FromJsonAuto[A](fromJson)
  }
}

trait Defaults[A] {
  type Out
  def apply(): Out
}

object Defaults {
  type Aux[A, O <: HList] = Defaults[A] { type Out = O }

  implicit def ignoreDefaults[A, O <: HList](implicit
      defaults: Default.AsOptions.Aux[A, O],
      defaultEraser: Mapper.Aux[eraseDefaults.type, O, O]
  ): Defaults.Aux[A, O] =
    new Defaults[A] {
      type Out = O
      def apply() = defaultEraser(defaults())
    }
}

private[ninny] object eraseDefaults extends Poly1 {
  implicit def erase[A]: Case.Aux[Option[A], Option[A]] = at(_ => None)
}
