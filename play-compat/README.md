# Play JSON compatibility

Have a third party library that you'd like to use that has support for Play but 
not ninny, or the other way around?  
No problem, `import io.github.kag0.ninny.compat.PlayToNinny._` or 
`io.github.kag0.ninny.compat.NinnyToPlay._` to convert one way or the other, 
or `import io.github.kag0.ninny.compat.PlayCompat._` to get both. 

> **note**: you need an instance of `ToSomeJson` to get a `Writes`

```scala
import io.github.kag0.ninny.compat.PlayCompat._

implicit val format: Format[MyType] = ???
myType.toSomeJson // 👍

val js: JsNumber = JsonNumber(123.456) // 👍
```

This makes it easy to migrate to ninny over time rather than having to re-write 
all your serialization and deserialization up-front.