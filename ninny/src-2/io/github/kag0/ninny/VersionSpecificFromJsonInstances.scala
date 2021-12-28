package io.github.kag0.ninny

import scala.util.Success
import scala.annotation.nowarn

import shapeless.::
import shapeless.{HList, HNil, Lazy, Nat, Sized, Succ}
import shapeless.Nat._0
import shapeless.ops.hlist._

trait VersionSpecificFromJsonInstances {

  /** provides a FromJson for a HList provided a list of field names of the same
    * length
    */
  implicit def recordFromJson[
      Head,
      Tail <: HList,
      TailLen <: Nat
  ](implicit
      headFromJson: Lazy[FromJson[Head]],
      @nowarn tailLenEv: Length.Aux[Tail, TailLen],
      tailFromJson: Sized[List[String], TailLen] => FromJson[Tail]
  ): Sized[List[String], Succ[TailLen]] => FromJson[Head :: Tail] = names =>
    FromJson.fromSome { json =>
      for {
        head <- headFromJson.value.from(json / names.head)
        tail <- tailFromJson(names.tail).from(json)
      } yield head :: tail
    }

  implicit val hNilFromJson: Sized[List[String], _0] => FromJson[HNil] = _ =>
    _ => Success(HNil)
}
