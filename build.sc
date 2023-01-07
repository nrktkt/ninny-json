import mill._
import scalalib._
import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill.define.{Segment, Segments}
import $file.forProductN

val `2.12` = "2.12.17"
val `2.13` = "2.13.10"
val `3`    = "3.1.0"

val scalaTest     = ivy"org.scalatest::scalatest:3.2.10"
val json4sVersion = "4.0.3"

trait PublishMod extends PublishModule {
  def sonatypeUri = "https://s01.oss.sonatype.org/service/local"
  def sonatypeSnapshotUri =
    "https://s01.oss.sonatype.org/content/repositories/snapshots"

  def artifactName =
    Segments(
      millModuleSegments.value.filterNot(_.isInstanceOf[Segment.Cross]): _*
    ).parts.mkString("-")

  def publishVersion = T.input(T.ctx().env("PUBLISH_VERSION"))
  def pomSettings =
    PomSettings(
      description = "NoneIsNotNullY",
      organization = "tk.nrktkt",
      url = "https://nrktkt.github.io/ninny-json/USERGUIDE",
      Seq(License.Unlicense),
      VersionControl.github("nrktkt", "ninny-json"),
      Seq(Developer("nrktkt", "Nathan Fischer", "https://github.com/nrktkt"))
    )
}

object ninny extends mill.Cross[Ninny](`2.12`, `2.13`, `3`)
class Ninny(val crossScalaVersion: String)
    extends CrossScalaModule
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
      ivy"org.typelevel::jawn-parser:1.3.0",
      ivy"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core:2.12.0",
      ivy"org.scala-lang.modules::scala-collection-compat:2.6.0",
      ivy"com.typesafe.scala-logging::scala-logging:3.9.4"
    ) ++ (if (crossScalaVersion != `3`)
            Agg(ivy"com.chuusai::shapeless:2.3.7")
          else None)

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

  object test extends Tests with TestModule.ScalaTest {
    def scalacOptions =
      T(self.scalacOptions().filterNot(_ == "-Xfatal-warnings"))
    def ivyDeps =
      Agg(
        ivy"org.json4s::json4s-native-core:$json4sVersion",
        ivy"org.slf4j:slf4j-simple:1.7.32",
        scalaTest
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
  def ivyDeps    = Agg(ivy"com.typesafe.play::play-json:2.9.2")

  object test extends Tests {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}

object `json4s-compat` extends mill.Cross[Json4sCompat](`2.12`, `2.13`)
class Json4sCompat(val crossScalaVersion: String)
    extends CrossScalaModule
    with PublishMod {

  def artifactName = "ninny-json4s-compat"

  def moduleDeps = List(ninny(crossScalaVersion))
  def ivyDeps    = Agg(ivy"org.json4s::json4s-ast:$json4sVersion")

  object test extends Tests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}

object ubjson extends ScalaModule with PublishMod {
  def scalaVersion = `2.13`
  def artifactName = "ninny-ubjson"
  def moduleDeps   = List(ninny(`2.13`))

  object test extends Tests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}

object `script-kit` extends mill.Cross[ScriptKit](`2.12`, `2.13`)
class ScriptKit(val crossScalaVersion: String)
    extends CrossScalaModule
    with PublishMod {

  def artifactName = "ninny-script-kit"

  def moduleDeps = List(ninny(crossScalaVersion))

  object test extends Tests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}
