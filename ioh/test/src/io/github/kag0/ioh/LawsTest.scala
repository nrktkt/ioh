package io.github.kag0.ioh

import cats.{Eq, Functor}
import org.scalatest.funsuite.{AnyFunSuite, AnyFunSuiteLike}
import org.typelevel.discipline.scalatest.Discipline
import cats.implicits._
import cats.laws.discipline.FunctorTests
import io.github.kag0.ioh._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.Configuration
import org.scalatest.prop.Configuration.PropertyCheckConfiguration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/*
object arbitraries {
  implicit def arbIOh[A: Arbitrary]: Arbitrary[IOh[A]] =
    Arbitrary(for {
      e <- Arbitrary.arbitrary[A]
    } yield () => Future.successful(e))
}

class LawsTest extends AnyFunSuite with Discipline with Configuration {
  implicit def eqIOh[A: Eq]: Eq[IOh[A]] = Eq.fromUniversalEquals

  checkAll("IOh.FunctorLaws", FunctorTests[IOh].functor[Int, Int, String])
}

 */
