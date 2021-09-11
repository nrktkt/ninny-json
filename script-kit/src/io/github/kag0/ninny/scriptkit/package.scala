package io.github.kag0.ninny

import scala.util.Success

package object scriptkit {
  implicit def fromMaybeSyntax(syntax: MaybeJsonSyntax) =
    new SkSyntax(Success(syntax.maybeJson))

  implicit def fromHopefullySyntax(syntax: HopefullyJsonSyntax) =
    new SkSyntax(syntax.hopefullyJson.map(Some(_)))

  implicit def fromHopefullyMaybeSyntax(syntax: HopefullyMaybeJsonSyntax) =
    new SkSyntax(syntax.maybeHopefullyJson)
}
