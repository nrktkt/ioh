package io.github.kag0.ioh

import java.lang.invoke.LambdaConversionException

import cats.effect.{Async, Bracket}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Try}

class Spec extends AnyFlatSpec with Matchers {
  implicit class Wait[A](f: Future[A]) {
    def await() = Await.result(f, 5 seconds)
  }

  "ioh instances" should "behave as a monad" in {
    val n = 5.pure[IOh]
    n.flatMap(i => IOh(i + 1))().await() shouldEqual 6
  }

  it should "raise errors" in {
    val ex = new LambdaConversionException("it broke")
    val o = ex.raiseError[IOh, Unit]
    a[LambdaConversionException] should be thrownBy {
      o()
        .andThen {
          case Failure(exception) => exception shouldEqual ex
          case _                  => fail()
        }
        .await()
    }
  }

  it should "behave async" in {
    val l = Async[IOh]
      .asyncF[String](cb => () => Future(cb(Left(new Exception("bar")))))

    val r = Async[IOh].async[String](cb => cb(Right("foo")))

    val comb = for {
      rr <- r
      ll <- l.recover { case out => out.getMessage }
    } yield rr + ' ' + ll

    comb().await() shouldEqual "foo bar"
  }

  it should "be referentially transparent" in {
    var x = 0

    val inc = IOh(x += 1)

    (for {
      _ <- inc
      _ <- inc
    } yield ())().await()

    var y = 0

    (for {
      _ <- IOh(y += 1)
      _ <- IOh(y += 1)
    } yield ())().await()

    x shouldEqual y
  }

  it should "bracket" in {
    var released = true

    def acquire = IOh {
      released = false
      IOh { released = true }
    }

    val b =
      Bracket[IOh, Throwable].bracket(acquire)(_ => {
        released shouldEqual false
        new Exception().raiseError[IOh, Unit]
      })(release => IOh { release() })

    Try(b().await())
    assert(released)
  }

  it should "add functionality to future" in {
    for {
      _ <- () => Future(println("do this"))
      _ <- () => Future("do that")
    } yield ()
  }
}
