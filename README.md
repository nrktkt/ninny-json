# None Is Not Null

ninny-json is an experiment to look at what JSON type classes would look like
if they made a distinction between absent JSON fields, and fields with `null` 
values.  
This project does include its own AST, but the point here is really not to 
introduce a new AST or look at the ergonomics of manipulating the AST directly.
Thus, the AST included is kept simple.  
Why not use json4s, the project created to provide one unifying AST? Read on.

## Why does this matter?

In principle, we want our libraries to be as expressive as possible.  
In practice, the limitations of libraries today make it hard or impossible to 
implement things like [JSON merge patch](https://tools.ietf.org/html/rfc7396).  
Whether a field will be included in the final JSON is also left up to the 
configuration of the JSON serializer (whether to include nulls or not) rather 
than the AST. When the AST doesn't match the JSON output, testability issues 
can open up.

## [Jump to proposal](#what-are-we-proposing)

## What do libraries do today? 

Let's look at three popular libraries and see how they deal with converting some
`Option[A]` to and from JSON.

### json4s

json4s uses the following type classes

```scala
trait Reader[T] {
  def read(value: JValue): T
}
trait Writer[-T] {
  def write(obj: T): JValue
}
```

These are fairly standard and pretty similar to Play JSON, with the difference 
that they throw exceptions.

Interestingly json4s includes a `JNothing` in its AST. Technically there is no 
such thing as "nothing" in JSON, but I can see how it would allow for maximum 
flexibility with other JSON libraries given that's the goal of json4s.

`JNothing` *would* let us distinguish between `None` and a missing field. 
However, the default `Writer[Option[A]]` doesn't leverage `JNothing`, 
rather it just writes `None` as `JNull`. 
The default reader for `Option` on the other hand just aggregates a failure to 
parse for any reason into `None`.

#### Pros

* It is technically possible to distinguish null from absent both when reading 
and writing JSON.

#### Cons

* Default readers/writers don't distinguish null from absent.
* `JNothing` makes for a strange AST. We can imagine bugs where 
    ```scala
    myObj.obj.map(_._1).contains("myField") // true
    // and yet
    myObj \ "myField" // JNothing
    ```
  Some might suggest 
  "well you should have done `myObj \ "myField" != JNothing` instead", 
  but ideally that's a mistake that wouldn't compile.
  
### Play JSON

Play JSON uses the type classes

```scala
trait Writes[A] { 
  def writes(o: A): JsValue
}
trait Reads[A] {
  def reads(json: JsValue): JsResult[A]
}
```

with a more standard AST.

It does provide a `Writes[Option[A]]`, which writes `None` as `null`. 
However, there is no `Reads[Option[A]]` since the type class has no way to know 
if the field was missing. 

The nice thing about Play JSON is the macro based type class derivation, so you 
can just write `implicit val format = Json.format[MyModel]`. 
Now you might think 
"well, that's not very useful if there is no `Reads[Option]`" 
and `MyModel` can't have any optional fields. 
However, that's not the case, and the macro generated code *will* read an 
`Option` using some internal logic. 
This works for the common use case, but if we want to distinguish between an 
absent field and some null, then we can't use the automatic format because we 
need access to the fields on the outer object.

```scala
Reads(root => JsSuccess(MyClass((root \ "myField") match {
  case JsDefined(JsNull) => Null
  case JsDefined(value) => Defined(value)
  case JsUndefined() => Absent
})))
```

#### Pros

* Automatic format derivation (although circe will call it semi-automatic)

#### Cons

* Inconsistent handling of `Option` between `Reads` and `Writes`.
* If we want to take direct control, we lose the composability of type classes.

### circe

circe uses the type classes

```scala
trait Encoder[A] { 
  def apply(a: A): Json
}
trait Decoder[A] {
  def apply(c: HCursor): Decoder.Result[A]
  def tryDecode(c: ACursor): Decoder.Result[A] = c match {
    case hc: HCursor => apply(hc)
    case _ =>
      Left(
        DecodingFailure("Attempt to decode value on failed cursor", c.history)
      )
  }
}
```

The `Encoder` here is the same as we've seen in the others (and it also encodes 
`None` as null), but the `Decoder` is interesting. Since circe uses cursors to 
move around the JSON, there is an `ACursor` which has the ability to tell us 
that the cursor was unable to focus on the field we're trying to decode (the 
field wasn't there). circe can and does use this to decode missing fields into 
`None`, and we can use it to distinguish null from absent fields.

```scala
new Decoder[FieldPresence[A]] {
  def tryDecode(c: ACursor) = c match {
    case c: HCursor =>
      if (c.value.isNull) Right(Null)
      else
        d(c) match {
          case Right(a) => Right(Some(Defined(a)))
          case Left(df) => Left(df)
        }
    case c: FailedCursor =>
      if (!c.incorrectFocus) Some(Absent) 
      else Left(DecodingFailure("[A]Option[A]", c.history))
  }
}
``` 
Because this is a `Decoder` for the value rather than the object containing the 
value, we can still use circe's awesome fully automatic type class generation 
which doesn't even require us to invoke a macro method like we do in Play.

Sadly there is nothing we can do with the `Encoder` to indicate that we don't 
want our field included in the output.

#### Pros

* `Decoder` can distinguish between null and absent fields.

#### Cons

* `Encoder` can't output an indication that the field should be absent.
* Cursors might be intimidating to the uninitiated.

## What are we proposing?

Now that we have the lay of the land, what are we proposing to shore up the 
cons without losing the pros?

Two simple type classes (the signatures are what matter, not the names) 

```scala
trait ToJson[A] {
  // return None if the field should not be included in the JSON
  def to(a: A): Option[JsonValue]
}
trait FromJson[A] {
  // None if the field was not present in the JSON
  def from(maybeJson: Option[JsonValue]): Try[A]
}
```
> note: `Try` and `Option` aren't strictly required, anything that conceptually 
> conveys the possibility of failure and absence will work.
 
`ToJson[Option[A]]` is implemented predictably
```scala
new ToJson[Option[A]] {
  def to(a: Option[A]) = a.flatMap(ToJson[A].to(_))
}
```

`FromJson[Option[A]]` is pretty straightforward as well
```scala
new FromJson[Option[A]] {
  def from(maybeJson: Option[JsonValue]) = maybeJson match {
    case Some(JsonNull) => Success(None)
    case Some(json)     => FromJson[A].from(json).map(Some(_))
    case None           => Success(None)
  }
}
```

If we want to distinguish between a null and absent field

```scala
new FromJson[FieldPresence[A]] {
  def from(maybeJson: Option[JsonValue]) = Success(maybeJson match {
    case Some(JsonNull) => Null
    case Some(json)     => Defined(json)
    case None           => Absent
  })
}
```

How are we doing with our pros and cons?

- [x] Able to distinguish null from absent fields when reading and writing JSON 
from inside the type class.
- [x] AST is predictable and closely models JSON.
- [x] Macros should still work, and they can be used even if we want to control 
field visibility based on a value type. 
(Although this project doesn't implement any macros).
- [x] `Option` is handled in the same way when reading and writing.

### Ergonomic improvements

Always dealing with `Option` could get annoying, 
so some simple additions can alleviate that

#### `FromJson`
Addition of a method that takes the AST directly saves us from having to 
constantly invoke `Some()`.
```scala
def from(json: JsonValue): Try[A]
```

#### `ToJson`
Some types (like `String`) will always result in a JSON output. Instances for 
those types can be implemented with `ToSomeJson` to remove the `Option` from 
created AST.

```scala
trait ToSomeJson[A] extends ToJson[A] {
  def toSome(a: A): JsonValue
  override def to(a: A) = Some(toSome(a))
}
```

### [Example](ninny/test/src/io/github/kag0/ninny/Example.scala)

An example of updating a user profile which clears one field, 
sets the value of another, and leaves a third unchanged without overwriting it 
with the existing value.
