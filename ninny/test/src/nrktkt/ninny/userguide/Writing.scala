package nrktkt.ninny.userguide

import nrktkt.ninny.ToSomeJson

object Writing {
// format: off

private implicit def noneToSomeJson: ToSomeJson[None.type] = ???

import nrktkt.ninny._

// convert values to json with .toJson
42.toJson     // Some(JsonNumber(42))

// use .toSomeJson to skip the Option
42.toSomeJson // JsonNumber(42)

// .toSomeJson doesn't compile unless the type 
// definitely produces a JSON value
None.toSomeJson 
//   ^
// could not find implicit value for parameter toJson: nrktkt.ninny.ToSomeJson[None.type]

//---

val json = obj(
  "firstName" ~> "John",
  "lastName"  ~> "Doe",
  "address" ~> obj(
    "street" ~> "710 Ashbury St",
    "zip"    ~> "94117"
  ),
  "kids" ~> arr("Jr", "Jane"),
  "age"  ~> None
)

Json.render(json) 
/* {
  "lastName":"Doe",
  "firstName":"John",
  "address":{
    "street":"710 Ashbury St",
    "zip":"94117"
  },
  "kids":["Jr","Jane"]
} */

trait GomJabbar
val gomJabbar = new GomJabbar {}

/*

implicit val gjToJson1: ToJson[GomJabbar] = ???
implicit val gjToJson2: ToJson[GomJabbar] = ???

obj("gom_jabbar" -> gomJabbar)
/*
type mismatch;
  found   : nrktkt.ninny.userguide.Writing.GomJabbar
  required: nrktkt.ninny.magnetic.JsonMagnet
obj("gom_jabbar" -> gomJabbar)
                    ^
*/

obj("gom_jabbar" ~> gomJabbar)
/*
ambiguous implicit values:
  both value gjToJson1 in object Writing of type => nrktkt.ninny.ToJson[GomJabbar]
  and value gjToJson2 in object Writing of type => nrktkt.ninny.ToJson[GomJabbar]
  match expected type nrktkt.ninny.package.ToJson[GomJabbar]
obj("gom_jabbar" ~> gomJabbar)
                 ^
*/

*/
}
