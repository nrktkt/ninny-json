
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kag0/ninny_2.13?style=for-the-badge)](https://mvnrepository.com/artifact/io.github.kag0/ninny)
[![coverage](https://img.shields.io/badge/test%20coverage-%3E%2090%25-brightgreen?style=for-the-badge)](https://kag0.github.io/ninny-json/coverage)  
[![Gitter](https://img.shields.io/gitter/room/kag0/ninny-json?style=for-the-badge)](https://gitter.im/kag0/ninny-json?utm_source=share-link&utm_medium=link&utm_campaign=share-link)
[![Matrix](https://img.shields.io/matrix/kag0_ninny-json:gitter.im?label=chat%20on%20matrix&style=for-the-badge&logoColor=0dbd8b)](https://matrix.to/#/#kag0_ninny-json:gitter.im?via=gitter.im&via=matrix.org)


# Integrations

* [Akka HTTP](https://github.com/hseeberger/akka-http-json)
* [blackdoor jose](https://blackdoor.github.io/jose/)
* [mercury JSON-RPC](https://github.com/lightform-oss/mercury/tree/master/ninny)
* [Play JSON](play-compat) (if you find something with Play support, it will work with ninny too!)

<script type='module' src="https://cdn.jsdelivr.net/gh/kag0/sauce@11.1.0/sauce.js">
  <h1>If you're reading this, you should go to the userguide website at https://kag0.github.io/ninny-json/USERGUIDE </h1>
</script>

# Reading values from JSON

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/Reading.scala'
    lines='8:63'
></sauce-code>

# Writing values to JSON
<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/Writing.scala'
    lines='9:23'
></sauce-code>

`obj` and `arr` build JSON structures

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/Writing.scala'
    lines='25:47'
></sauce-code>

# Updating nested values

With immutable ASTs it can be a pain to update values deep inside the tree.  
You can use ninny's dynamic update syntax easly to replace values way down in there.

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/Updating.scala'
    lines='5:18'
></sauce-code>

# Converting domain objects to JSON

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/DomainTo.scala'
    lines='7:51'
></sauce-code>

# Converting JSON to domain objects

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/DomainFrom.scala'
    lines='22:49'
></sauce-code>

## Semi-auto derivation for `ToJson` and `FromJson`

Rather than implementing `ToJson` and `FromJson` by hand, you can generate them 
automatically using

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/SemiAuto.scala'
    lines='10:16'
></sauce-code>

## Full-auto derivation for `ToJson` and `FromJson`

If you like you can even skip the declaration by mixing in `AutoToJson` or 
`AutoFromJson`, or importing `io.github.kag0.ninny.Auto._`.

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/FullAuto.scala'
    lines='8:18'
></sauce-code>

## `forProductN` derivation for `toJson` and `FromJson`

Often you want to have different names in a case class than what's in the JSON, but you don't need to change the types at all. `forProductN` methods are perfect for this, just specify what the JSON field name should be and provide the case class `apply`/`unapply` method to generate a `ToJson`, `FromJson`, or both together.

<sauce-code 
    repo='kag0/ninny'
    lang='scala'
    file='ninny/test/src/io/github/kag0/ninny/userguide/ForProductN.scala'
    lines='8:24'
></sauce-code>
