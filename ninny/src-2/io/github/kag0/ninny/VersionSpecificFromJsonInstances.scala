package io.github.kag0.ninny

import scala.util.Success
import scala.annotation.nowarn

import shapeless.::
//import shapeless.labelled.{FieldType, field}
import shapeless.{HList, HNil, Lazy, Nat, Sized, Succ}
import shapeless.Nat._0
import shapeless.ops.hlist._
//import shapeless.ops.nat._
//import shapeless.ops.sized._

trait VersionSpecificFromJsonInstances {
  implicit def recordFromJson[
      Head,
      Tail <: HList,
      TailLen <: Nat
  ](implicit
      headFromJson: Lazy[FromJson[Head]],
      @nowarn tailLenEv: Length.Aux[Tail, TailLen],
      tailFromJson: Sized[List[String], TailLen] => FromJson[Tail]
  ): Sized[List[String], Succ[TailLen]] => FromJson[Head :: Tail] = names => {
    // val key  = witness.value
    // val name = key.name
    // val name :: names = allNames

    FromJson.fromSome[Head :: Tail] { json =>
      val maybeHeadJson = json / names.head
      for {
        head <- headFromJson.value.from(maybeHeadJson)
        tail <- tailFromJson(names.tail).from(json)
      } yield head :: tail
    }
  }

  implicit val hNilFromJson: Sized[List[String], _0] => FromJson[HNil] = _ =>
    _ => Success(HNil)
}
