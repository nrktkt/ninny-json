package io.github.kag0.ninny

import scala.util.Success
import scala.annotation.nowarn

import shapeless.::
import shapeless.{HList, HNil, Lazy, Nat, Sized, Succ}
import shapeless.Nat._0
import shapeless.ops.hlist._

import io.github.kag0.ninny.ast.JsonValue
import scala.util.Failure

trait VersionSpecificFromJsonInstances {

  /** provides a FromJson for a HList provided a list of field names of the same
    * length
    */
  implicit def recordFromJson[
      Head,
      DefaultHead <: Option[Head],
      Tail <: HList,
      DefaultTail <: HList,
      TailLen <: Nat
  ](implicit
      headFromJson: Lazy[FromJson[Head]],
      @nowarn tailLenEv: Length.Aux[Tail, TailLen],
      tailFromJson: (
          Sized[List[String], TailLen],
          DefaultTail
      ) => FromJson[Tail]
  ): (
      Sized[List[String], Succ[TailLen]],
      DefaultHead :: DefaultTail
  ) => FromJson[Head :: Tail] = (names, defaults) => {
    val defaultHeadFromJson: FromJson[Head] = maybe =>
      (maybe, defaults.head) match {
        case (maybe: Some[JsonValue], _) => headFromJson.value.from(maybe)
        case (_, default: Some[Head])    => Success(default.value)
        case _                           => headFromJson.value.from(None)
      }

    FromJson.fromSome { json =>
      for {
        head <- defaultHeadFromJson
          .from(json / names.head)
          .recoverWith {
            case e: JsonFieldException =>
              Failure(
                new JsonFieldException(
                  e.message,
                  s"${names.head}.${e.field}",
                  e
                )
              )
            case e: Exception =>
              Failure(new JsonFieldException(e.getMessage, names.head, e))
          }
        tail <- tailFromJson(names.tail, defaults.tail).from(json)
      } yield head :: tail
    }
  }

  private val sNil = Success(HNil)
  implicit val hNilFromJson: (Sized[List[String], _0], HNil) => FromJson[HNil] =
    (_, _) => _ => sNil
}
