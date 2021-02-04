[![Maven Central](https://img.shields.io/maven-central/v/io.github.kag0/ninny_2.13?style=for-the-badge)](https://mvnrepository.com/artifact/io.github.kag0/ninny)
[![coverage](https://img.shields.io/badge/test%20coverage-%3E%2090%25-brightgreen?style=for-the-badge)](https://kag0.github.io/ninny-json/coverage)

# Integrations

* [Akka HTTP](https://github.com/hseeberger/akka-http-json)
* [blackdoor jose](https://blackdoor.github.io/jose/)
* [mercury JSON-RPC](https://github.com/lightform-oss/mercury/tree/master/ninny)
* [Play JSON](play-compat) (if you find something with Play support, it will work with ninny too!)

# Reading values from JSON
<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Reading.scala?slice=7:62">If you're reading this, you should go to the userguide website at https://kag0.github.io/ninny-json/USERGUIDE </script>

# Writing values to JSON

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Writing.scala?slice=8:23"></script>

`obj` and `arr` build JSON structures

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Writing.scala?slice=25:47"></script>

# Updating nested values

With immutable ASTs it can be a pain to update values deep inside the tree.  
You can use ninny's dynamic update syntax easly to replace values way down in there.

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/Updating.scala?slice=4:17"></script>

# Converting domain objects to JSON

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/DomainTo.scala?slice=6:50"></script>

# Converting JSON to domain objects

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/DomainFrom.scala?slice=9:47"></script>

## Semi-auto derivation for `ToJson` and `FromJson`

Rather than implementing `ToJson` and `FromJson` by hand, you can generate them 
automatically using

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/SemiAuto.scala?slice=8:15"></script>

## Full-auto derivation for `ToJson` and `FromJson`

If you like you can even skip the declaration by mixing in `AutoToJson` or 
`AutoFromJson`, or importing `io.github.kag0.ninny.Auto._`.

<script src="https://gist-it.appspot.com/github/kag0/ninny-json/blob/master/ninny/test/src/io/github/kag0/ninny/userguide/FullAuto.scala?slice=7:17"></script>

<script>
  (function () {
    var links = document.querySelectorAll("link[href='http://gist-it.appspot.com/assets/embed.css'], link[href='http://gist-it.appspot.com/assets/prettify/prettify.css']");
    links.forEach(function (link, index) {
        console.log(index, link);
        link.parentNode.removeChild(link);
    }); 
}()); 
</script>
