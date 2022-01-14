package nrktkt.ninny.binary

import scala.collection.immutable.ArraySeq
import nrktkt.ninny._

class Doc extends App {

// format: OFF
{
val binary = ArraySeq[Byte]('S', 'i', 12, 
  'H','e','l','l','o',' ','W','o','r','l','d','!'
)

import nrktkt.ninny.binary.UbJson
UbJson.parse(binary).to[String] // Success(Hello World!)

}
{

val array: Array[Byte] = ???
val immutableArray = ArraySeq.unsafeWrapArray(array)
UbJson.parse(immutableArray)

}

// format: ON
}
