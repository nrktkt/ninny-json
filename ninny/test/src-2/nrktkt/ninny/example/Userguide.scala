package nrktkt.ninny.example

import nrktkt.ninny.ast.JsonValue

import scala.util.{Failure, Try}
import scala.collection.immutable._
// format: off
object Userguide extends App { 
import nrktkt.ninny._

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
json / "firstName" // Some(JsonString("John"))

// also works on Try[JsonValue]
hopefullyJson / "firstName" // Success(Some(JsonString("John")))

// .to[Type] converts values from json to scala
(json / "firstName").to[String] // Success(John)

// arrays are accessed with the same syntax
json / "kids" / 1 // Some(JsonString("Jane"))

// out of bound indexes are handled
json / "kids" / 2 // None

// works on nested objects
json / "address" / "zip" // Some(JsonString("94117"))

// missing fields are handled
json / "age" // None

// you can use dynamic syntax directly on a JsonValue
json.firstName.to[String] // Success(John)

// Try[JsonValue] needs a .* before beginning to use dynamic syntax
hopefullyJson.*.firstName.to[String] // Success(John)

// type conversion failures work as you'd expect
json.firstName.to[Boolean]
// Failure(nrktkt.ninny.JsonException: Expected boolean, got "John")

// dynamic array syntax works predictably
json.kids(1).to[String] // Success(Jane)

json.address.zip.to[String] // Success(94117)

// no special method for optional fields, just change the target type
json.age.to[Option[String]] // Success(None)

///////////////////////////////////////////////////////////////////////////////

// convert values to json with .toJson
42.toJson // Some(JsonNumber(42))

// use .toSomeJson to skip the Option
42.toSomeJson // JsonNumber(42)

// .toSomeJson doesn't work unless the type definitely produces a JSON value
//None.toSomeJson // could not find implicit value for parameter toJson: nrktkt.ninny.ToSomeJson[None.type]

////////////////////////

val json1 = obj(
  "firstName" ~> "John",
  "lastName"  ~> "Doe",
  "address" ~> obj(
    "street" ~> "710 Ashbury St",
    "zip"    ~> "94117"
  ),
  "kids" ~> arr("Jr", "Jane"),
  "age"  ~> None
)

Json.render(json1) // {"lastName":"Doe","firstName":"John","address":{"street":"710 Ashbury St","zip":"94117"},"kids":["Jr","Jane"]}

////////////////////////////////////////////

val deep = obj(
  "one" ~> obj(
    "two" ~> obj(
      "three" ~> "value!"
    )
  )
)

deep.withUpdated.one.two.three := "new value!" // {"one":{"two":{"three":"new value!"}}}

/////////////////////////////////////////////////////////////////////////////

case class Person(
    firstName: String,
    lastName: String,
    address: Address,
    kids: Seq[String],
    age: Option[Int]
)

case class Address(street: String, zip: String)

// manual
{
/*
  implement ToSomeJson instead of ToJson if your object always produces
  some kind of JSON. this is a common case.
  */
implicit val addressToJson: ToSomeJson[Address] = a =>
  obj(
    "street" ~> a.street,
    "zip"    ~> a.zip
  )

implicit val addressFromJson: FromJson[Address] = {
  case None => Failure(new NoSuchElementException())
  case Some(json) =>
    for {
      first <- json.street.to[String]
      last  <- json.zip.to[String]
    } yield Address(first, last)
}

implicit val personToJson: ToSomeJson[Person] = p =>
  obj(
    "firstName" ~> p.firstName,
    "lastName"  ~> p.lastName,
    "address"   ~> p.address,
    "kids"      ~> p.kids,
    "age"       ~> p.age
  )

implicit val personFromJson = FromJson.fromSome[Person](json =>
  for {
    first   <- json.firstName.to[String]
    last    <- json.lastName.to[String]
    address <- json.address.to[Address]
    kids    <- json.kids.to[Seq[String]]
    age     <- json.age.to[Option[Int]]
  } yield Person(first, last, address, kids, age)
)

Person(
  "John",
  "Doe",
  Address("710 Ashbury St", "94117"),
  Seq("Jr", "Jane"),
  age = None
).toSomeJson: JsonValue

Json.parse("").to[Person]: Try[Person]
}

// semi auto
{
  /*
    implement ToSomeJson instead of ToJson if your object always produces
    some kind of JSON. this is a common case.
    */
  implicit val addressToJson = ToAndFromJson.auto[Address]

  implicit val personToJson = ToJson.auto[Person]

  implicit val personFromJson = FromJson.auto[Person]

  Person(
    "John",
    "Doe",
    Address("710 Ashbury St", "94117"),
    Seq("Jr", "Jane"),
    age = None
  ).toSomeJson: JsonValue

  Json.parse("").to[Person]: Try[Person]
}

// full auto
{
  import nrktkt.ninny.Auto._

  Person(
    "John",
    "Doe",
    Address("710 Ashbury St", "94117"),
    Seq("Jr", "Jane"),
    age = None
  ).toSomeJson: JsonValue

  Json.parse("").to[Person]: Try[Person]
}
}
