# [UBJSON](https://ubjson.org) binary support

This module supports converting the AST to and from Universal Binary JSON.

Usage is idential to standard JSON, with the exception that `io.github.kag0.ninny.Json` is replaced with `io.github.kag0.ninny.binary.UbJson`.

For example

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ubjson/test/src/io/github/kag0/ninny/binary/ubjson/Doc.scala?slice=13:16"></script>

**Gotchas**

Ninny has a dedicated `JsonBlob` type which doesn't map exactly to UBJSON's semantics.

```
UBJSON     ninny           JSON
[$U#  <->  JsonBlob    ->  string
S     <-   JsonString  <-  string
```
> `[$U#` is a UBJSON typed array of `uint8`, `S` is the UBJSON string type.

In effect

```scala
UbJson.render(Json.parse(Json.render(UbJson.parse(someTypedUInt8Array)))) != someTypedUInt8Array
```

However in practice this is rarely an issue, and never an issue when dealing with classes. eg.

```scala
case class ByteContainer(value: ArraySeq[Bytes])
UbJson.render(Json.parse(byteContainer).to[ByteContainer])
```

correctly outputs `value` as a typed array of `uint8`.