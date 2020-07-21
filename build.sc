import mill._, scalalib._

import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule

def scalaV = "2.13.3"

object ninny extends ScoverageModule {
  def scalaVersion = scalaV
  def ivyDeps = Agg(ivy"org.typelevel::jawn-parser:1.0.0")
  object test extends ScoverageTests {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps = Agg(
      ivy"org.json4s::json4s-native:3.6.9",
      ivy"org.scalatest::scalatest:3.2.0"
    )
  }
  def scoverageVersion = "1.4.1"
}
