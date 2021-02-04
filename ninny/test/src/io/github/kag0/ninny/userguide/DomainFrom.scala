package io.github.kag0.ninny.userguide

import scala.util.Failure
import scala.util.Try

object DomainFrom {

val personString = """{"lastName":"Doe","firstName":"John","address":{"street":"710 Ashbury St","zip":"94117"},"kids":["Jr","Jane"]}"""

import io.github.kag0.ninny._

case class Person(
    firstName: String,
    lastName: String,
    address: Address,
    kids: Seq[String],
    age: Option[Int]
)
case class Address(street: String, zip: String)

object Address {
  implicit val fromJson: FromJson[Address] = {
    case None => Failure(new NoSuchElementException())
    case Some(json) =>
      for {
        first <- json.street.to[String]
        last  <- json.zip.to[String]
      } yield Address(first, last)
  }
}

object Person {
  // use FromJson.fromSome for convenience to skip handling absent fields
  implicit val fromJson = 
    FromJson.fromSome[Person](json =>
      for {
        first   <- json.firstName.to[String]
        last    <- json.lastName.to[String]
        address <- json.address.to[Address]
        kids    <- json.kids.to[Seq[String]]
        age     <- json.age.to[Option[Int]]
      } yield Person(first, last, address, kids, age)
    )
}

Json.parse(personString).to[Person]: Try[Person]

}
