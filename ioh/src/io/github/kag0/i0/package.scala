package io.github.kag0

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

package object i0 {
  //type I0[-Dependency, +Error, +A] = Dependency => Future[Either[Error, A]]
  //type Reader[-Dependency, +A] = I0[Dependency, Nothing, A]
  //type Effect[+A]              = I0[Any, Nothing, A]

  final class PartiallyApplied[D](val dummy: Unit = ()) extends AnyVal {
    def apply[DD, E, A](i0: D => I0[DD, E, A]) =
      I0[D with DD, E, A](dd => i0(dd).run(dd))
  }

  object I0 {

    implicit def apply[D, E, A](i0: D => Future[Either[E, A]]) = new I0(i0)

    def fromDepAsync[D, A](reader: D => Future[A])(implicit
        ec: ExecutionContext
    ): I0[D, Nothing, A] = new I0(reader.andThen(_.map(Right(_))))

    def fromDepTyped[D, E, A](value: D => Either[E, A]): I0[D, E, A] =
      I0(value.andThen(Future.successful))

    def pure[A](a: A) = new I0[Any, Nothing, A](_ =>
      Future.successful(Right(a))
    )

    implicit def async[A](a: => Future[A])(implicit ec: ExecutionContext) =
      new I0[Any, Nothing, A](_ => a.map(Right(_)))

    def error[E](e: E) = new I0[Any, E, Nothing](_ =>
      Future.successful(Left(e))
    )

    def panic(e: Throwable) = I0[Any, Nothing, Nothing](_ => Future.failed(e))

    def using[D] = new PartiallyApplied[D]

    // squint, hmmm
    implicit def fromLazy[A](a: => A): I0[Any, Nothing, A] =
      new I0((_: Any) => Future.successful(Right(a)))

    /*
    implicit def fromSyncDependencyWError[D, E, A](
        fn: D => Either[E, A]
    ): I0[D, E, A] = fn.andThen(Future.successful)

    implicit def fromAsyncDependency[D, A](fn: D => Future[A])(implicit
        ec: ExecutionContext
    ): I0[D, Nothing, A] = fn.andThen(_.map(Right(_)))

    implicit def fromFun[D, A](fn: D => A): I0[D, Nothing, A] =
      I0(d => Future.successful(Right(fn(d))))

    def pure[A](a: A): I0[Any, Nothing, A]             = _ => a

     */
  }

  final class I0[-Dependency, +Error, +A](
      val run: Dependency => Future[Either[Error, A]]
  ) extends AnyVal {

    def map[B](fn: A => B)(implicit
        ec: ExecutionContext
    ): I0[Dependency, Error, B] = run.andThen(_.map(_.map(fn)))

    def asyncMap[B](fn: A => Future[B])(implicit ec: ExecutionContext) =
      run.andThen(_.flatMap(_.fold(Future.successful, fn)))

    def errorMap[EE >: Error, B](fn: A => Either[EE, B])(implicit
        ec: ExecutionContext
    ) = run.andThen(_.map(_.flatMap(fn)))

    def flatMap[DD <: Dependency, EE >: Error, B](
        fn: A => I0[DD, EE, B]
    )(implicit ec: ExecutionContext): I0[DD, EE, B] =
      new I0(dd =>
        run(dd).flatMap {
          case Left(e)  => Future.successful(Left(e))
          case Right(a) => fn(a).run(dd)
        }
      )

    def flatten[DD <: Dependency, EE >: Error, B](implicit
        ec: ExecutionContext,
        ev: A <:< I0[DD, EE, B]
    ): I0[DD, EE, B] = flatMap(ev)

    def filter(predicate: A => Boolean)(implicit ec: ExecutionContext) =
      withFilter(predicate)

    def withFilter(
        predicate: A => Boolean
    )(implicit ec: ExecutionContext): I0[Dependency, Error, A] =
      new I0(d =>
        run(d).withFilter {
          case Right(a) => predicate(a)
          case _        => false
        }
      )

    def provide(dependency: Dependency) =
      I0[Any, Error, A](_ => run(dependency))
  }

  /*
  implicit class ReaderSyntax[D, A](val self: D => A) extends AnyVal {
    def i0: I0[D, Nothing, A] = self
  }

  implicit class SyncDependencyWErrorSyntax[D, E, A](
      val self: D => Either[E, A]
  ) extends AnyVal {
    def i0: I0[D, E, A] = self
  }

  implicit class AsyncDependencySyntax[D, A](val self: D => Future[A])
      extends AnyVal {
    def i0(implicit ec: ExecutionContext): I0[D, Nothing, A] = self
  }

  implicit def fromReader[D, A](reader: D => A): I0[D, Nothing, A] =
    reader.andThen(a => Future.successful(Right(a)))
   */

  val double = (i: Int) => i * 2

  val res = I0.using[String](s => s.length)

  import scala.concurrent.ExecutionContext.Implicits.global
  //val io = for {
  //i <- I0.pure(1)
  //twoI = i * 2
  //d <- ((i: Int) => i * 2).i0
  //x <- I0.pure(4) if d % 2 == 0
  //} yield x

  //io.runAsync(1)
}
