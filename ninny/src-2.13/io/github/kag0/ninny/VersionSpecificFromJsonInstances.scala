package io.github.kag0.ninny

import shapeless.::
import shapeless.labelled.{FieldType, field}
import shapeless.{HList, HNil, Lazy, Witness}
import scala.util.Success

trait VersionSpecificFromJsonInstances {

  implicit def recordFromJson[Key <: Symbol, Head, Tail <: HList](implicit
      witness: Witness.Aux[Key],
      headFromJson: Lazy[FromJson[Head]],
      tailFromJson: FromJson[Tail]
  ): FromJson[FieldType[Key, Head] :: Tail] = {
    val key  = witness.value
    val name = key.name

    FromJson.fromSome[FieldType[Key, Head] :: Tail] { json =>
      val maybeHeadJson = json / name
      for {
        head <- headFromJson.value.from(maybeHeadJson)
        tail <- tailFromJson.from(json)
      } yield field[Key](head) :: tail
    }
  }

  implicit val hNilFromJson: FromJson[HNil] = _ => Success(HNil)
}
