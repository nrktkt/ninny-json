package nrktkt.ninny.userguide

import nrktkt.ninny.userguide.DomainFrom.Address
import nrktkt.ninny.userguide.DomainFrom.Person

object SemiAuto {
// format: off

import nrktkt.ninny._

// generate ToJson and FromJson at the same time with ToAndFromJson
implicit val toAndFromJson = ToAndFromJson.auto[Address]
// or generate them separately
implicit val fromJson = FromJson.auto[Person]
implicit val toJson = ToJson.auto[Person]

}
