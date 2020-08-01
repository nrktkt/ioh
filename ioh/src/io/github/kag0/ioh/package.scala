package io.github.kag0

import cats.Parallel
import cats.effect.{Effect, ExitCase, IO, SyncIO}

import scala.concurrent.{ExecutionContext, Future, Promise}

package object ioh {

  type IOh[A] = () => Future[A]

  object IOh {
    def apply[A](suspend: => A): IOh[A] = () => Future.successful(suspend)
    def unit: IOh[Unit] = () => Future.successful(())
  }

  implicit def iOhInstances(implicit ec: ExecutionContext): Effect[IOh] =
    new Effect[IOh] {
      def tailRecM[A, B](a: A)(f: A => IOh[Either[A, B]]): IOh[B] =
        flatMap(f(a)) {
          case Left(a)  => tailRecM(a)(f)
          case Right(b) => pure(b)
        }

      def flatMap[A, B](fa: IOh[A])(f: A => IOh[B]) =
        () => fa().flatMap(f(_)())

      def pure[A](x: A) = () => Future.successful(x)

      def raiseError[A](e: Throwable) = () => Future.failed(e)

      def handleErrorWith[A](fa: IOh[A])(f: Throwable => IOh[A]) =
        () => fa().recoverWith(f(_)())

      def async[A](k: (Either[Throwable, A] => Unit) => Unit) = () => {
        val p = Promise[A]
        Future(k(outcome => p.tryComplete(outcome.toTry)))
        p.future
      }

      def asyncF[A](k: (Either[Throwable, A] => Unit) => IOh[Unit]) = {
        val p = Promise[A]
        flatMap(k(outcome => p.tryComplete(outcome.toTry)))(_ => () => p.future)
      }

      /**
        * does not guard against throwing in <code>use</code>
        */
      def bracketCase[A, B](acquire: IOh[A])(use: A => IOh[B])(
          release: (A, ExitCase[Throwable]) => IOh[Unit]
      ): IOh[B] = flatMap(acquire) { a =>
        onError(
          flatMap(use(a))(b => map(release(a, ExitCase.complete))(_ => b))
        ) {
          case e => release(a, ExitCase.error(e))
        }
      }

      def suspend[A](thunk: => IOh[A]): IOh[A] = () => thunk()

      def runAsync[A](fa: IOh[A])(cb: Either[Throwable, A] => IO[Unit]) =
        SyncIO(fa().onComplete(r => cb(r.toEither).unsafeRunAsyncAndForget()))
    }

  implicit def iOhParallel(
      implicit ec: ExecutionContext
  ): Parallel.Aux[IOh, IOh] =
    Parallel.identity[IOh]
}
