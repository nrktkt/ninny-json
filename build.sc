import mill._
import scalalib._
import $ivy.`com.lihaoyi::mill-contrib-scoverage:$MILL_VERSION`
import mill.contrib.scoverage.ScoverageModule
import mill.scalalib.publish._
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill.define.{Segment, Segments}

val `2.12` = "2.12.12"
val `2.13` = "2.13.3"

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
      ivy"org.scala-lang.modules::scala-collection-compat:2.4.1"
    )

  override def generatedSources =
    T {
      generateSources()
      Seq(PathRef(T.dest / os.up / os.up / "generateSources" / "dest"))
    }

  def generateSources =
    T {
      os.write(T.dest / "ProductToJson.scala", generateProductToJson())
      os.write(T.dest / "ProductFromJson.scala", generateProductFromJson())
      os.write(
        T.dest / "ProductToAndFromJson.scala",
        generateProductToAndFromJson()
      )

    }

  def generateProductToJson =
    T {
      val out = new StringBuilder()
      out ++= "package io.github.kag0.ninny\n"
      out ++= "trait ProductToJson {\n"
      for (i <- 1 to 22) {

        out ++= s"def forProduct${i}[Target, "
        val aTypes = (0 until i).map(j => s"A$j").mkString(", ")
        out ++= aTypes
        out ++= s"]("
        out ++= (0 until i).map(j => s"nameA$j: String").mkString(", ")
        out ++= s")(f: Target => ("
        out ++= aTypes
        out ++= "))(implicit "
        out ++=
          (0 until i)
            .map(j => s"a${j}ToJson: ToJson[A$j]")
            .mkString(", ")

        out ++= "): ToSomeJsonObject[Target] = target => {\n"
        out ++= "val ("
        out ++= (0 until i).map(j => s"a$j").mkString(", ")
        out ++= ") = f(target)\n"
        out ++= "obj("
        out ++= (0 until i).map(j => s"(nameA$j, a$j)").mkString(", ")
        out ++= ")\n}\n"
      }

      out += '}'
      out.result()
    }

  def generateProductFromJson =
    T {
      val out = new StringBuilder()
      out ++= "package io.github.kag0.ninny\n"
      out ++= "trait ProductFromJson {\n"
      for (i <- 1 to 22) {

        out ++= s"def forProduct${i}[Target, "
        val aTypes = (0 until i).map(j => s"A$j").mkString(", ")
        out ++= aTypes
        out ++= s"]("
        out ++= (0 until i).map(j => s"nameA$j: String").mkString(", ")
        out ++= s")(f: ("
        out ++= aTypes
        out ++= ") => Target)(implicit "
        out ++=
          (0 until i).map(j => s"a${j}FromJson: FromJson[A$j]").mkString(", ")

        out ++= "): FromJson[Target] = json => for {\n"
        out ++=
          (0 until i)
            .map(j => s"a$j <- (json / nameA$j).to[A$j]")
            .mkString("\n")

        out ++= "}\nyield f("
        out ++= (0 until i).map(j => s"a$j").mkString(", ")
        out ++= ")\n"
      }

      out += '}'
      out.result()
    }

  def generateProductToAndFromJson =
    T {
      val out = new StringBuilder()
      out ++= "package io.github.kag0.ninny\n"
      out ++= "import io.github.kag0.ninny.ast._\n"
      out ++= "trait ProductToAndFromJson {\n"
      for (i <- 1 to 22) {

        out ++= s"def forProduct${i}[Target, "
        val aTypes = (0 until i).map(j => s"A$j").mkString(", ")
        out ++= aTypes
        out ++= s"]("
        out ++= (0 until i).map(j => s"nameA$j: String").mkString(", ")
        out ++= s")(fFrom: ("
        out ++= aTypes
        out ++= ") => Target,\n"
        out ++= "fTo: Target => ("
        out ++= s"$aTypes))(implicit "
        out ++=
          (0 until i)
            .map(j => s"a${j}ToAndFromJson: ToAndFromJson[A$j]")
            .mkString(", ")

        out ++= ") = new ToAndFromJson[Target] {\n"
        val namesCsv = (0 until i).map(j => s"nameA$j").mkString(", ")
        out ++= s"val _toJson: ToJson[Target] = ToJson.forProduct$i($namesCsv)(fTo)\n"
        out ++= s"val _fromJson: FromJson[Target] = FromJson.forProduct$i($namesCsv)(fFrom)\n"

        out ++= """
          def from(json: Option[JsonValue]) = _fromJson.from(json)
          def to(target: Target)            = _toJson.to(target)
          """
        out ++= "}\n"
      }

      out += '}'
      out.result()
    }

  object test extends ScoverageTests {
    def scalacOptions  = self.scalacOptions().filterNot(_ == "-Xfatal-warnings")
    def testFrameworks = Seq("org.scalatest.tools.Framework")
    def ivyDeps =
      Agg(
        ivy"org.json4s::json4s-native:3.6.9",
        ivy"org.scalatest::scalatest:3.2.0"
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
