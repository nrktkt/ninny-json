import mill._
import scalalib._
import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill.define.{Segment, Segments}

val `2.12` = "2.12.12"
val `2.13` = "2.13.3"
val `3`    = "3.0.0"

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

object ninny extends mill.Cross[Ninny](`2.12`, `2.13`, `3`)
class Ninny(val crossScalaVersion: String)
    extends CrossScalaModule
    //with ScoverageModule
    with PublishMod { self =>

  def scalacOptions =
    Seq(
      "-Xfatal-warnings",
      "-feature",
      "-unchecked",
      "-deprecation"
    ) ++ (if (crossScalaVersion != `3`)
            Seq("-Ywarn-macros:after", "-Ywarn-unused")
          else None)

  def ivyDeps =
    Agg(
      ivy"org.typelevel::jawn-parser:1.1.2",
      ivy"org.scala-lang.modules::scala-collection-compat:2.4.4"
    ) ++ (if (crossScalaVersion != `3`)
            Agg(ivy"com.chuusai::shapeless:2.3.3")
          else None)

  object test extends Tests with TestModule.ScalaTest { //extends ScoverageTests {
    def scalacOptions =
      T(self.scalacOptions().filterNot(_ == "-Xfatal-warnings"))
    //zdef testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps =
      Agg(
        ivy"org.json4s::json4s-native-core:4.0.1",
        ivy"org.scalatest::scalatest:3.2.9"
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
