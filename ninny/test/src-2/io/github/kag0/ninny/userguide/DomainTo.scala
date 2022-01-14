package io.github.kag0.ninny.userguide

import io.github.kag0.ninny.ast.JsonValue

object DomainTo extends App {
// format: off

import nrktkt.ninny._

case class Person(
  firstName: String,
  lastName: String,
  address: Address,
  kids: Seq[String],
  age: Option[Int]
)

case class Address(street: String, zip: String)

object Address {
  /*
   implement ToSomeJson instead of ToJson if your object 
   always produces some kind of JSON. 
   this is a common case.
   */
  implicit val toJson: ToSomeJson[Address] = a =>
    obj(
      "street" -> a.street,
      "zip"    -> a.zip
    )
}

object Person {
  implicit val toJson: ToSomeJson[Person] = p =>
    obj(
      "firstName" -> p.firstName,
      "lastName"  -> p.lastName,
      "address"   -> p.address,
      "kids"      -> p.kids,
      "age"       -> p.age
    )
}

Person(
  "John",
  "Doe",
  Address("710 Ashbury St", "94117"),
  Seq("Jr", "Jane"),
  age = None
).toSomeJson: JsonValue

}
