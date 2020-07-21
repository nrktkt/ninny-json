package io.github.kag0.ninny

import java.lang.Character.UnicodeBlock

import scala.language.dynamics

package object ast {
  sealed trait JsonValue extends Dynamic {
    def selectDynamic(name: String)        = MaybeJsonSyntax(/(name))
    def apply(i: Int)                      = MaybeJsonSyntax(/(i))
    def applyDynamic(name: String)(i: Int) = selectDynamic(name)(i)

    def /(name: String): Option[JsonValue] =
      this match {
        case JsonObject(values) => values.get(name)
        case _                  => None
      }

    def /(i: Int): Option[JsonValue] =
      this match {
        case JsonArray(values) if values.length > i && i >= 0 => Some(values(i))
        case _                                                => None
      }

    def to[A: FromJson] = FromJson[A].from(this)

    override def toString =
      this match {
        case JsonNull           => "null"
        case JsonBoolean(value) => value.toString
        case JsonNumber(value)  => value.toString.stripSuffix(".0")
        case s: JsonString      => s.toString
        case JsonArray(values)  => values.mkString("[", ",", "]")
        case JsonObject(values) =>
          values
            .map { case (k, v) => s"${JsonString.escape(k)}:$v" }
            .mkString("{", ",", "}")
      }
  }

  case class JsonObject(values: Map[String, JsonValue]) extends JsonValue
  case class JsonArray(values: Seq[JsonValue])          extends JsonValue
  case class JsonNumber(value: Double)                  extends JsonValue
  case class JsonBoolean(value: Boolean)                extends JsonValue
  case object JsonNull                                  extends JsonValue

  case class JsonString(value: String) extends JsonValue {
    override def toString = JsonString.escape(value)
  }
  object JsonString {
    def escape(s: String): String =
      s"${'"'}${s.map {
        case '"'  => "\\\""
        case '\\' => """\\"""
        case '/'  => """\/"""
        case '\b' => """\b"""
        case '\f' => """\f"""
        case '\n' => """\n"""
        case '\r' => """\r"""
        case '\t' => """\t"""
        case c if UnicodeBlock.of(c) != UnicodeBlock.BASIC_LATIN =>
          s"\\u${String.format("%04x", c: Int)}"
        case c => c
      }.mkString}${'"'}"
  }
}
