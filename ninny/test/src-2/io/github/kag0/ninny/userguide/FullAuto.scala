package io.github.kag0.ninny.userguide

import io.github.kag0.ninny.userguide.DomainFrom.Person
import io.github.kag0.ninny.userguide.DomainFrom.Address

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
