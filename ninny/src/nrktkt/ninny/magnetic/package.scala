package nrktkt.ninny

import nrktkt.ninny.ast._
import scala.collection.immutable._
import scala.language.implicitConversions
import nrktkt.ninny.magnetic.JsonMagnet
import nrktkt.ninny.magnetic.SomeJsonMagnet

package object magnetic extends MagneticMethods {

  type JsonMagnet = JsonMagnet.Type
  object JsonMagnet {
    type Base
    trait Tag extends Any
    type Type = Base with Tag

    implicit def apply[A: ToJson](a: A): JsonMagnet =
      (if (null == a) None else a.toJson).asInstanceOf[JsonMagnet]

    implicit def unapply(arg: JsonMagnet): Option[JsonValue] =
      arg.asInstanceOf[Option[JsonValue]]
  }

  type SomeJsonMagnet = SomeJsonMagnet.Type
  object SomeJsonMagnet {
    type Base
    trait Tag extends Any
    type Type = Base with Tag

    implicit def apply[A: ToSomeJson](a: A): SomeJsonMagnet =
      (if (null == a) None else a.toSomeJson).asInstanceOf[SomeJsonMagnet]

    implicit def unapply(magnet: SomeJsonMagnet): JsonValue =
      magnet.asInstanceOf[JsonValue]
  }
}

trait MagneticMethods {
  def obj(nameValues: (String, JsonMagnet)*) =
    JsonObject(
      nameValues.collect { case (name, JsonMagnet(json)) =>
        name -> json
      }.toMap
    )

  def arr(values: SomeJsonMagnet*) =
    JsonArray(Seq(values.map(SomeJsonMagnet.unapply): _*))
}
