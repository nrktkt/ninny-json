package nrktkt.ninny

import scala.collection.immutable.AbstractMap
import nrktkt.ninny.ast.JsonValue
import scala.collection.immutable.TreeMap

class RoMap(
  wrapped: scala.collection.mutable.Map[String, JsonValue]
) extends AbstractMap[String, JsonValue] with VersionSpecificRoMapMethods {
  def iterator = wrapped.iterator
  def get(key: String) = wrapped.get(key)
  def removed(key: String) = toTreeMap - key
  override def updated[V1 >: JsonValue](key: String, value: V1) = toTreeMap.updated(key, value)
  override def +[V1 >: JsonValue](kv: (String, V1)) = updated(kv._1, kv._2)
  def toTreeMap = {
    val builder = TreeMap.newBuilder[String, JsonValue]
    wrapped.foreach(kv => builder += kv)
    builder.result()
  } 
}
