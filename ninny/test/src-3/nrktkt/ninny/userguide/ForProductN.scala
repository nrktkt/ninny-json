package nrktkt.ninny.userguide

import nrktkt.ninny._

object ForProductN extends App {
// format: Off

case class Address(street: String, zip: String)

val fromJson: FromJson[Address] =
  FromJson.forProduct2("street", "zip_code")(Address.apply)

implicit 
val toJson: ToSomeJson[Address] =
  ToJson.forProduct2("street", "zip_code")({ address =>
    val Address(street, zip) = Address.unapply(address)
    (street, zip)
  })

val toAndFromJson: ToAndFromJson[Address] =
  ToAndFromJson.forProduct2[Address, String, String]("street", "zip_code")(
    Address.apply, { address =>
      val Address(street, zip) = Address.unapply(address)
      (street, zip)
    }
  )

Address(street = "710 Ashbury St", zip = "94117").toSomeJson
// {"street":"710 Ashbury St","zip_code":"94117"}

}
