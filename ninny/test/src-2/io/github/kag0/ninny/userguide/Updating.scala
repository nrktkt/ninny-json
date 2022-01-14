package io.github.kag0.ninny.userguide

object Updating {
// format: off

import nrktkt.ninny._

val json = obj(
  "one" -> obj(
    "two" -> obj(
      "three" -> "value!"
    )
  )
)

json.withUpdated.one.two.three := "new value!" 
// {"one":{"two":{"three":"new value!"}}}

}
