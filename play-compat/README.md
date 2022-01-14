# Play JSON compatibility

Have a third party library that you'd like to use that has support for Play but 
not ninny, or the other way around?  
No problem, `import nrktkt.ninny.compat.PlayToNinny._` or 
`nrktkt.ninny.compat.NinnyToPlay._` to convert one way or the other, 
or `import nrktkt.ninny.compat.PlayCompat._` to get both. 

> **note**: you need an instance of `ToSomeJson` to get a `Writes`

```scala
import nrktkt.ninny.compat.PlayCompat._

implicit val format: Format[MyType] = ???
myType.toSomeJson // 👍

val js: JsNumber = JsonNumber(123.456) // 👍
```

This makes it easy to migrate to ninny over time rather than having to re-write 
all your serialization and deserialization up-front.