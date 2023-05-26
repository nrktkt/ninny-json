package nrktkt.ninny.userguide

import nrktkt.ninny._

object ForProductN extends App {
// format: Off

case class Address(street: String, zip: String)

val fromJson: FromJson[Address] =
  FromJson.forProduct2("street", "zip_code")(Address.apply)

implicit 
val toJson: ToSomeJson[Address] =
  ToJson.forProduct2("street", "zip_code")(Address.unapply(_).get)

val toAndFromJson: ToAndFromJson[Address] =
  ToAndFromJson.forProduct2("street", "zip_code")(
    Address.apply, Address.unapply(_).get
  )

Address(street = "710 Ashbury St", zip = "94117").toSomeJson
// {"street":"710 Ashbury St","zip_code":"94117"}

}
