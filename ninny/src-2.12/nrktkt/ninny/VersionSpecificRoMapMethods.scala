package nrktkt.ninny

trait VersionSpecificRoMapMethods {
  this: RoMap => 
  override def -(key: String) = removed(key)
}
