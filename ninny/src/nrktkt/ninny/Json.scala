package nrktkt.ninny

import nrktkt.ninny.ast.JsonValue
import org.typelevel.jawn.Parser
import nrktkt.ninny.jawn._
import nrktkt.ninny.jsoniter.NinnyJsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala
import scala.collection.compat.immutable.ArraySeq
import scala.util.Try

object Json {
  def parse(s: String, highPrecision: Boolean = false): Try[JsonValue] =
    Parser.parseFromString(s)(
      if (highPrecision) DecimalFacade else DoubleFacade
    )

  def parseArray(
      s: ArraySeq[Byte],
      highPrecision: Boolean = false
  ): Try[JsonValue] =
    Parser.parseFromByteArray(s.unsafeArray.asInstanceOf[Array[Byte]])(
      if (highPrecision) DecimalFacade else DoubleFacade
    )

  def render(json: JsonValue): String =
    jsoniter_scala.core.writeToString(json)(NinnyJsonValueCodec)
}
