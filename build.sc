import mill._
import scalalib._
import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill.define.{Segment, Segments}
import $file.forProductN

val `2.12` = "2.12.20"
val `2.13` = "2.13.16"
val `3`    = "3.3.4"

val scalaTest     = mvn"org.scalatest::scalatest:3.2.10"
val json4sVersion = Map(4 -> "4.0.6", 3 -> "3.6.12")

trait PublishMod extends PublishModule {
  def sonatypeUri = "https://s01.oss.sonatype.org/service/local"
  def sonatypeSnapshotUri =
    "https://s01.oss.sonatype.org/content/repositories/snapshots"

  // artifactName defined per-module to avoid Segments API differences

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

object ninny extends mill.Cross[Ninny](Seq(`2.12`, `2.13`, `3`))
trait Ninny
    extends CrossScalaModule
    with PublishMod
    with mill.Cross.Module[String] { self =>

  override def crossScalaVersion: String = crossValue
  def artifactName                       = "ninny"

  def scalacOptions =
    Seq(
      "-Xfatal-warnings",
      "-feature",
      "-unchecked",
      "-deprecation"
    ) ++ (
      if (crossScalaVersion != `3`) Seq("-Ywarn-macros:after", "-Ywarn-unused")
      else None
    ) ++ (
      if (crossScalaVersion == `2.12`) Seq("-language:higherKinds")
      else None
    )

  def ivyDeps =
    Agg(
      mvn"org.typelevel::jawn-parser:1.3.0",
      mvn"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core:2.12.0",
      mvn"org.scala-lang.modules::scala-collection-compat:2.6.0",
      mvn"com.typesafe.scala-logging::scala-logging:3.9.4"
    ) ++ (if (crossScalaVersion != `3`)
            Agg(mvn"com.chuusai::shapeless:2.3.7")
          else None)

  override def generatedSources =
    T {
      Seq(generateSources())
    }

  def generateSources =
    T {
      val out = T.dest
      os.makeDir.all(out)
      os.write(
        out / "ProductToJson.scala",
        forProductN.generateProductToJson
      )
      os.write(
        out / "ProductFromJson.scala",
        forProductN.generateProductFromJson
      )
      os.write(
        out / "ProductToAndFromJson.scala",
        forProductN.generateProductToAndFromJson
      )
      PathRef(out)
    }

  object test extends ScalaTests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def scalacOptions =
      T(self.scalacOptions().filterNot(_ == "-Xfatal-warnings"))
    def ivyDeps =
      Agg(
        mvn"org.json4s::json4s-native-core:${json4sVersion(4)}",
        mvn"org.slf4j:slf4j-simple:1.7.32",
        mvn"com.google.guava:guava:33.4.0-jre",
        scalaTest
      )
  }

  def scoverageVersion = "1.4.1"
}

object `old-namespace`
    extends mill.Cross[OldNamespace](Seq(`2.12`, `2.13`, `3`))
trait OldNamespace
    extends CrossScalaModule
    with PublishMod
    with mill.Cross.Module[String] {
  override def crossScalaVersion: String = crossValue
  def artifactName                       = "ninny-old-namespace"
  def moduleDeps                         = List(ninny(crossScalaVersion))
}

object `play-compat` extends mill.Cross[PlayCompat](Seq(`2.12`, `2.13`))
trait PlayCompat
    extends CrossScalaModule
    with PublishMod
    with mill.Cross.Module[String] {
  override def crossScalaVersion: String = crossValue

  def artifactName = "ninny-play-compat"

  def moduleDeps = List(ninny(crossScalaVersion))
  def ivyDeps    = Agg(mvn"com.typesafe.play::play-json:2.9.4")

  object test extends ScalaTests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}

object `json4s-compat` extends mill.Module {
  trait Json4sCompatBase
      extends CrossScalaModule
      with PublishMod
      with mill.Cross.Module[String] {
    override def crossScalaVersion: String = crossValue
    protected def json4sMajor: Int

    def millSourcePath = super.millSourcePath / os.up
    def artifactName   = s"ninny-json4s$json4sMajor-compat"
    def moduleDeps     = List(ninny(crossScalaVersion))
    def ivyDeps = Agg(mvn"org.json4s::json4s-ast:${json4sVersion(json4sMajor)}")

    object test extends ScalaTests with TestModule.ScalaTest {
      def testFrameworks = Seq("org.scalatest.tools.Framework")
      def ivyDeps        = Agg(scalaTest)
    }
  }

  object v4 extends mill.Cross[Json4sCompatV4](Seq(`2.12`, `2.13`, `3`))
  trait Json4sCompatV4 extends Json4sCompatBase {
    override protected def json4sMajor: Int = 4
  }

  object v3 extends mill.Cross[Json4sCompatV3](Seq(`2.12`, `2.13`))
  trait Json4sCompatV3 extends Json4sCompatBase {
    override protected def json4sMajor: Int = 3
  }
}

object `circe-compat` extends mill.Cross[CirceCompat](Seq(`2.12`, `2.13`))
trait CirceCompat
    extends CrossScalaModule
    with PublishMod
    with mill.Cross.Module[String] {
  override def crossScalaVersion: String = crossValue
  def artifactName                       = "ninny-circe-compat"

  def moduleDeps = List(ninny(crossScalaVersion))
  def ivyDeps = Agg(
    mvn"io.circe::circe-core:0.14.3",
    mvn"io.circe::circe-generic:0.14.3",
    mvn"io.circe::circe-generic-extras:0.14.3"
  )

  object test extends ScalaTests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}

object ubjson extends ScalaModule with PublishMod {
  def scalaVersion = `2.13`
  def artifactName = "ninny-ubjson"
  def moduleDeps   = List(ninny(`2.13`))

  object test extends ScalaTests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}

object `script-kit` extends mill.Cross[ScriptKit](Seq(`2.12`, `2.13`))
trait ScriptKit
    extends CrossScalaModule
    with PublishMod
    with mill.Cross.Module[String] {
  override def crossScalaVersion: String = crossValue

  def artifactName = "ninny-script-kit"

  def moduleDeps = List(ninny(crossScalaVersion))

  object test extends ScalaTests with TestModule.ScalaTest {
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps        = Agg(scalaTest)
  }
}
