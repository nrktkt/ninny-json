import mill._
import scalalib._
import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule
import mill.scalalib.publish._

def scalaV = "2.13.3"

object ninny extends ScoverageModule with PublishModule {
  def scalaVersion = scalaV
  def ivyDeps = Agg(
    ivy"org.typelevel::jawn-parser:1.0.0",
    ivy"com.chuusai::shapeless:2.3.3"
  )

  object test extends ScoverageTests {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps = Agg(
      ivy"org.json4s::json4s-native:3.6.9",
      ivy"org.scalatest::scalatest:3.2.0"
    )
  }

  def scoverageVersion = "1.4.1"

  def publishVersion = "0.1.0"
  def pomSettings = PomSettings(
    description = "NoneIsNotNullY",
    organization = "io.github.kag0",
    url = "https://github.com/kag0/ninny-json",
    Seq(License.Unlicense),
    VersionControl.github("kag0", "ninny-json"),
    Seq(Developer("kag0", "Nathan Fischer", "https://github.com/kag0"))
  )
}
