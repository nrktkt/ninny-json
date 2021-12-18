package io.github.kag0.ninny

trait VersionSpecificRoMapMethods {
  this: RoMap => 
  override def -(key: String) = removed(key)
}
