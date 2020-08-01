package io.github.kag0.ioh

import cats._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
// import cats._

sealed trait Tree[+A]
// defined trait Tree

case object Leaf extends Tree[Nothing]
// defined object Leaf

case class Node[A](p: A, left: Tree[A], right: Tree[A]) extends Tree[A]
// defined class Node

object Tree {
  implicit val functorTree: Functor[Tree] = new Functor[Tree] {
    def map[A, B](tree: Tree[A])(f: A => B) = tree match {
      case Leaf                 => Leaf
      case Node(p, left, right) => Node(f(p), map(left)(f), map(right)(f))
    }
  }
}
import org.scalacheck.{Arbitrary, Gen}

object arbitraries2 {
  implicit def arbTree[A: Arbitrary]: Arbitrary[Tree[A]] =
    Arbitrary(Gen.oneOf(Gen.const(Leaf), (for {
      e <- Arbitrary.arbitrary[A]
    } yield Node(e, Leaf, Leaf))))
}
/*

import cats.implicits._
// import cats.implicits._

import cats.laws.discipline.FunctorTests
// import cats.laws.discipline.FunctorTests

import org.scalatest.funsuite.AnyFunSuite
// import org.scalatest.funsuite.AnyFunSuite

import org.typelevel.discipline.scalatest.Discipline
// import org.typelevel.discipline.scalatest.Discipline

import arbitraries2._
// import arbitraries._

class TreeLawTests
    extends AnyFunSuite
    with Discipline
    with ScalaCheckPropertyChecks {
  implicit def eqTree[A: Eq]: Eq[Tree[A]] = Eq.fromUniversalEquals
  checkAll("Tree.FunctorLaws", FunctorTests[Tree].functor[Int, Int, String])
}


 */
