package io.github.kag0.ninny

import io.github.kag0.ninny.ast.JsonValue
import org.typelevel.jawn.Parser
import io.github.kag0.ninny.jawn._
import io.github.kag0.ninny.jsoniter.NinnyJsonValueCodec
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
