package io.github.kag0.ninny.userguide

import io.github.kag0.ninny.ToSomeJson

object Writing {
// format: off

private implicit def noneToSomeJson: ToSomeJson[None.type] = ???

import io.github.kag0.ninny._

// convert values to json with .toJson
42.toJson     // Some(JsonNumber(42))

// use .toSomeJson to skip the Option
42.toSomeJson // JsonNumber(42)

// .toSomeJson doesn't compile unless the type 
// definitely produces a JSON value
None.toSomeJson 
//   ^
// could not find implicit value for parameter toJson: io.github.kag0.ninny.ToSomeJson[None.type]

//---

val json = obj(
  "firstName" -> "John",
  "lastName"  -> "Doe",
  "address" -> obj(
    "street" -> "710 Ashbury St",
    "zip"    -> "94117"
  ),
  "kids" -> arr("Jr", "Jane"),
  "age"  -> None
)

json.toString 
/* {
  "lastName":"Doe",
  "firstName":"John",
  "address":{
    "street":"710 Ashbury St",
    "zip":"94117"
  },
  "kids":["Jr","Jane"]
} */

}
