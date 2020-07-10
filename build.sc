import mill._, scalalib._

def scalaV = "2.13.3"

object ninny extends ScalaModule {
  def scalaVersion = scalaV
  object test extends ScalaModule {
    def scalaVersion = scalaV
    def moduleDeps = Seq(ninny)
    def ivyDeps = Agg(
      ivy"org.json4s::json4s-native:3.6.9"
    )

  }
}
