package nrktkt.ninny.userguide

import nrktkt.ninny.ToAndFromJson

import nrktkt.ninny._

object Annotations extends App {
// format: Off

case class Example(
  @JsonName("string_field")
  stringField: String,
  int: Int
)

implicit val toFromJson = ToAndFromJson.auto[Example]

Example("foo", 1).toSomeJson
// {"string_field":"foo", "int":1}

// format: On
}
