package io.github.kag0

import cats.Parallel.Aux
import cats.effect.{Async, Bracket, ExitCase}
import cats.{Parallel, StackSafeMonad}

import scala.concurrent.{ExecutionContext, Future, Promise}

package object ioh {

  type IOh[A] = () => Future[A]

  object IOh {
    def apply[A](suspend: => A): IOh[A] = () => Future.successful(suspend)
  }

  implicit def iOhInstances(
    implicit ec: ExecutionContext
  ): StackSafeMonad[IOh] with Bracket[IOh, Throwable] with Async[IOh] =
    new StackSafeMonad[IOh] with Bracket[IOh, Throwable] with Async[IOh] {
      def flatMap[A, B](fa: IOh[A])(f: A => IOh[B]) =
        () => fa().flatMap(f(_)())

      def pure[A](x: A) = () => Future.successful(x)

      def raiseError[A](e: Throwable) = () => Future.failed(e)

      def handleErrorWith[A](fa: IOh[A])(f: Throwable => IOh[A]) =
        () => fa().recoverWith(f(_)())

      def async[A](k: (Either[Throwable, A] => Unit) => Unit) = () => {
        val p = Promise[A]
        Future(k(outcome => p.complete(outcome.toTry)))
        p.future
      }

      def asyncF[A](k: (Either[Throwable, A] => Unit) => IOh[Unit]) = {
        val p = Promise[A]
        flatMap(k(outcome => p.complete(outcome.toTry)))(_ => () => p.future)
      }

      /**
        * does not guard against throwing in <code>use</code>
        * @param acquire
        * @param use
        * @param release
        * @tparam A
        * @tparam B
        * @return
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
    }

  implicit def iOhParallel(implicit ec: ExecutionContext): Aux[IOh, IOh] =
    Parallel.identity[IOh]
}
