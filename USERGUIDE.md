
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kag0/ninny_2.13?style=for-the-badge)](https://mvnrepository.com/artifact/io.github.kag0/ninny)
[![coverage](https://img.shields.io/badge/test%20coverage-%3E%2090%25-brightgreen?style=for-the-badge)](https://kag0.github.io/ninny-json/coverage)  
[![Gitter](https://img.shields.io/gitter/room/kag0/ninny-json?style=for-the-badge)](https://gitter.im/kag0/ninny-json?utm_source=share-link&utm_medium=link&utm_campaign=share-link)
[![Matrix](https://img.shields.io/matrix/kag0_ninny-json:gitter.im?label=chat%20on%20matrix&style=for-the-badge&logoColor=0dbd8b)](https://matrix.to/#/#kag0_ninny-json:gitter.im?via=gitter.im&via=matrix.org)


# Integrations

* [Akka HTTP](https://github.com/hseeberger/akka-http-json)
* [blackdoor jose](https://blackdoor.github.io/jose/)
* [mercury JSON-RPC](https://github.com/lightform-oss/mercury/tree/master/ninny)
* [Play JSON](play-compat) (if you find something with Play support, it will work with ninny too!)

<link href="https://jmblog.github.io/color-themes-for-google-code-prettify/themes/tomorrow.css" type="text/css" rel="stylesheet" />
<style>
.gist-it-gist .gist-file .gist-meta,
.gister-gist .gist-file .gist-meta {
    overflow: hidden;
    padding: 0.5em;
}

.gist-it-gist .gist-file .gist-data,
.gister-gist .gist-file .gist-data {
    overflow: auto;
    word-wrap: normal;
}

.gist-it-gist .gist-file .gist-data pre,
.gister-gist .gist-file .gist-data pre {
    margin: 0 !important;
    padding: 0.5em;
}
</style>

# Reading values from JSON
<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Reading.scala?slice=8:63">  
  If you're reading this, you should go to the userguide website at https://kag0.github.io/ninny-json/USERGUIDE   
</script>

# Writing values to JSON

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Writing.scala?slice=9:23"></script>

`obj` and `arr` build JSON structures

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Writing.scala?slice=25:47"></script>

# Updating nested values

With immutable ASTs it can be a pain to update values deep inside the tree.  
You can use ninny's dynamic update syntax easly to replace values way down in there.

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Updating.scala?slice=5:18"></script>

# Converting domain objects to JSON

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/DomainTo.scala?slice=7:51"></script>

# Converting JSON to domain objects

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/DomainFrom.scala?slice=22:49"></script>

## Semi-auto derivation for `ToJson` and `FromJson`

Rather than implementing `ToJson` and `FromJson` by hand, you can generate them 
automatically using

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/SemiAuto.scala?slice=10:16"></script>

## Full-auto derivation for `ToJson` and `FromJson`

If you like you can even skip the declaration by mixing in `AutoToJson` or 
`AutoFromJson`, or importing `io.github.kag0.ninny.Auto._`.

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/FullAuto.scala?slice=8:18"></script>

## `forProductN` derivation for `toJson` and `FromJson`

Often you want to have different names in a case class than what's in the JSON, but you don't need to change the types at all. `forProductN` methods are perfect for this, just specify what the JSON field name should be and provide the case class `apply`/`unapply` method to generate a `ToJson`, `FromJson`, or both together.

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/ForProductN.scala?slice=8:24"></script>

<script>
  (function () {
    var links = document.querySelectorAll("link[href='https://gist-it.appspot.com/assets/embed.css'], link[href='https://gist-it.appspot.com/assets/prettify/prettify.css']");
    links.forEach(function (link, index) {
        console.log(index, link);
        link.parentNode.removeChild(link);
    }); 
}()); 
</script>
