package nrktkt.ninny

import scala.util.Success
import scala.annotation.nowarn
import scala.compiletime.ops.int.{+, -, S}

import nrktkt.ninny.ast.JsonValue
import scala.util.Failure
import scala.deriving.Mirror
import scala.util.Try

trait VersionSpecificFromJsonInstances {

  implicit def recordFromJson[
      Head,
      DefaultHead <: Option[Head],
      Tail <: Tuple,
      DefaultTail <: Tuple,
      TailLen <: Numlike
  ](implicit
      headFromJson: FromJson[Head],
      tailFromJson: (
          Sized[List[String], TailLen],
          DefaultTail
      ) => FromJson[Tail]
  ): (
      Sized[List[String], Added[TailLen]],
      DefaultHead *: DefaultTail
  ) => FromJson[Head *: Tail] = (names, defaults) => {
    val defaultHeadFromJson: FromJson[Head] = maybe =>
      (maybe, defaults.head) match {
        case (maybe: Some[JsonValue], _) => headFromJson.from(maybe)
        case (_, default: Some[Head])    => Success(default.value)
        case _                           => headFromJson.from(None)
      }

    FromJson.fromSome { json =>
      for {
        head <- defaultHeadFromJson
          .from(json / Sized.value(names).head)
          .recoverWith {
            case e: JsonFieldException =>
              Failure(
                new JsonFieldException(
                  e.message,
                  s"${Sized.value(names).head}.${e.field}",
                  e
                )
              )
            case e: Exception =>
              Failure(new JsonFieldException(e.getMessage, Sized.value(names).head, e))
          }
        tail <- tailFromJson(names.tail, defaults.tail).from(json)
      } yield head *: tail
    }
  }

  implicit val emptyTupleFromJson: ((Sized[List[String], Zero]), EmptyTuple) => FromJson[EmptyTuple] =
    (_, _) => _ => Success(EmptyTuple)
}

opaque type Sized[l <: List[String], Size] = List[String]

object Sized {
  def value[L <: List[String], Size](a: Sized[L, Size]): List[String] = a
  inline def apply[T <: Product, OverridingNames <: Tuple, Size <: Numlike](mirror: Mirror.ProductOf[T]): Sized[List[String], Size] =
    scala.compiletime.constValueTuple[Auto.OverrideNames[mirror.MirroredElemLabels, OverridingNames]].asInstanceOf[Tuple].toList.asInstanceOf[List[String]]
}

extension [L <: List[String], Size <: Numlike] (s: Sized[L, Added[Size]]) def tail: Sized[L, Size] = s.tail


type Numlike
type Added[T] <: Numlike
type Zero <: Numlike