package nrktkt.ninny.userguide

object Renaming extends App {
// format: off

import nrktkt.ninny._
import nrktkt.ninny.ast._

case class Person(
  firstName: String,
  lastName: String,
  address: Address,
  kids: Seq[String],
  age: Option[Int]
)

case class Address(houseNumber: Int, street: String, zip: String)

implicit val addressToJson: ToJson[Address] = ToJson.auto[Address]

import com.google.common.base.CaseFormat._

implicit val personToJson: ToJson[Person] = 
    ToJson.auto[Person].postprocess(
      (_: JsonObject).mapNames(LOWER_CAMEL.to(LOWER_UNDERSCORE, _))
    )

// format: on

  println(
    Person(
      "Jim",
      "Bob",
      Address(718, "Ashbury St.", "94117"),
      Nil,
      None
    ).toJson
  )
}
