package io.github.kag0.ninny.userguide

import scala.util.Try
import io.github.kag0.ninny.ast.JsonValue

object Reading {
// format: off

import io.github.kag0.ninny._

val hopefullyJson: Try[JsonValue] = Json.parse("""
{
"firstName": "John",
"lastName": "Doe",
"address": {
  "street": "710 Ashbury St",
  "zip": "94117"
},
"kids": ["Jr", "Jane"]
}
""")

val json: JsonValue = hopefullyJson.get

// select fields from the json
json / "firstName"              // Some(JsonString("John"))

// also works on Try[JsonValue]
hopefullyJson / "firstName"     // Success(Some(JsonString("John")))

// .to[Type] converts values from json to scala
(json / "firstName").to[String] // Success(John)

// arrays are accessed with the same syntax
json / "kids" / 1               // Some(JsonString("Jane"))

// out of bound indexes are handled
json / "kids" / 2               // None

// works on nested objects
json / "address" / "zip"        // Some(JsonString("94117"))

// missing fields are handled
json / "age"                    // None

// you can use dynamic syntax directly on a JsonValue
json.firstName.to[String]       // Success(John)

// Try[JsonValue] needs a .* before beginning to use dynamic syntax
hopefullyJson.*.firstName.to[String] // Success(John)

// type conversion failures work as you'd expect
json.firstName.to[Boolean]
// Failure(io.github.kag0.ninny.JsonException: Expected boolean, got "John")

// dynamic array syntax works predictably 
json.kids(1).to[String]     // Success(Jane)

json.address.zip.to[String] // Success(94117)

// no special method for optional fields, just change the target type
json.age.to[Option[String]] // Success(None)

}
