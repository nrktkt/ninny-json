import mill._
import scalalib._
import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill.define.{Segment, Segments}
import $file.forProductN

val `2.12` = "2.12.12"
val `2.13` = "2.13.6"

trait PublishMod extends PublishModule {
  def artifactName =
    Segments(
      millModuleSegments.value.filterNot(_.isInstanceOf[Segment.Cross]): _*
    ).parts.mkString("-")

  def publishVersion = T.input(T.ctx().env("PUBLISH_VERSION"))
  def pomSettings =
    PomSettings(
      description = "NoneIsNotNullY",
      organization = "io.github.kag0",
      url = "https://github.com/kag0/ninny-json",
      Seq(License.Unlicense),
      VersionControl.github("kag0", "ninny-json"),
      Seq(Developer("kag0", "Nathan Fischer", "https://github.com/kag0"))
    )
}

object ninny extends mill.Cross[Ninny](`2.12`, `2.13`)
class Ninny(val crossScalaVersion: String)
    extends CrossScalaModule
    with ScoverageModule
    with PublishMod { self =>

  def scalacOptions =
    Seq(
      "-Xfatal-warnings",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Ywarn-macros:after",
      "-Ywarn-unused"
    )

  def ivyDeps =
    Agg(
      ivy"org.typelevel::jawn-parser:1.0.0",
      ivy"com.chuusai::shapeless:2.3.3",
      ivy"org.scala-lang.modules::scala-collection-compat:2.4.1",
      ivy"com.typesafe.scala-logging::scala-logging:3.9.4"
    )

  override def generatedSources =
    T {
      generateSources()
      Seq(PathRef(T.dest / os.up / os.up / "generateSources" / "dest"))
    }

  def generateSources =
    T {
      os.write(
        T.dest / "ProductToJson.scala",
        forProductN.generateProductToJson
      )
      os.write(
        T.dest / "ProductFromJson.scala",
        forProductN.generateProductFromJson
      )
      os.write(
        T.dest / "ProductToAndFromJson.scala",
        forProductN.generateProductToAndFromJson
      )
    }

  object test extends ScoverageTests {
    def scalacOptions  = self.scalacOptions().filterNot(_ == "-Xfatal-warnings")
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps =
      Agg(
        ivy"org.json4s::json4s-native:3.6.9",
        ivy"org.scalatest::scalatest:3.2.0",
        ivy"org.slf4j:slf4j-simple:1.7.32"
      )
  }

  def scoverageVersion = "1.4.1"
}

object `play-compat` extends mill.Cross[PlayCompat](`2.12`, `2.13`)
class PlayCompat(val crossScalaVersion: String)
    extends CrossScalaModule
    with PublishMod {

  def artifactName = "ninny-play-compat"

  def moduleDeps = List(ninny(crossScalaVersion))
  def ivyDeps    = Agg(ivy"com.typesafe.play::play-json:2.9.1")

  object test extends Tests {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(ivy"org.scalatest::scalatest:3.2.0")
  }
}
