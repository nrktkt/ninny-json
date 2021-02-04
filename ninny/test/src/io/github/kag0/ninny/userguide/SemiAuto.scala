package io.github.kag0.ninny.userguide

import io.github.kag0.ninny.userguide.DomainFrom.Address
import io.github.kag0.ninny.userguide.DomainFrom.Person

object SemiAuto {

import io.github.kag0.ninny._

// generate ToJson and FromJson at the same time with ToAndFromJson
implicit val toAndFromJson = ToAndFromJson.auto[Address]
// or generate them separately
implicit val fromJson = FromJson.auto[Person]
implicit val toJson = ToJson.auto[Person]

}
