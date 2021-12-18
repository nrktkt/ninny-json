package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonObject
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, Lazy, Witness}

trait VersionSpecificToJsonInstances {

  implicit def recordToJson[Key <: Symbol, Head, Tail <: HList](implicit
      witness: Witness.Aux[Key],
      headToJson: Lazy[ToJson[Head]],
      tailToJson: ToSomeJsonObject[Tail]
  ): ToSomeJsonObject[FieldType[Key, Head] :: Tail] = {
    val name = witness.value.name
    ToJson { record =>
      val maybeHead = headToJson.value.to(record.head)
      val tail      = tailToJson.toSome(record.tail)

      maybeHead match {
        case Some(head) => tail + (name -> head)
        case None       => tail
      }
    }
  }

  implicit val hNilToJson: ToSomeJsonObject[HNil] = _ => JsonObject(Map.empty)
}
