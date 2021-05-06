import mill._
import mill.scalalib.publish._
import scalalib._

object ioh extends ScalaModule with PublishModule {
  def scalaVersion = "2.13.6"

  def publishVersion = "0.1.0"

  val catsV = "2.1.1"
  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:$catsV",
    ivy"org.typelevel::cats-effect:$catsV"
  )

  object test extends Tests {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.1.1",
      ivy"org.typelevel::cats-laws:$catsV",
      ivy"org.typelevel::discipline-scalatest:2.0.0"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

  def pomSettings = PomSettings(
    description = "() => Future(println(\"oh\"))",
    organization = "io.github.kag0",
    url = "https://github.com/kag0/ioh",
    Seq(License.Unlicense),
    VersionControl.github("kag0", "ioh"),
    Seq(Developer("kag0", "Nathan Fischer", "https://github.com/kag0"))
  )
}
