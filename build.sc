import mill._, scalalib._

object ioh extends ScalaModule {
  def scalaVersion = "2.13.1"
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
}
