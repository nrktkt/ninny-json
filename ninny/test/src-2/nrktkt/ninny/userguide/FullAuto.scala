package nrktkt.ninny.userguide

import nrktkt.ninny.userguide.DomainFrom.Person
import nrktkt.ninny.userguide.DomainFrom.Address

object FullAuto {
// format: off

import nrktkt.ninny.Auto._

Person(
  "John",
  "Doe",
  Address("710 Ashbury St", "94117"),
  Seq("Jr", "Jane"),
  age = None
).toSomeJson // just works

}
