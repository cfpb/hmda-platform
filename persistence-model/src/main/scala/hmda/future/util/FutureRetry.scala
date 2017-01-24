package hmda.future.util

import akka.actor.Scheduler
import akka.pattern.after

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

object FutureRetry {
  def retry[T](f: => Future[T], delay: Seq[FiniteDuration], retries: Int, defaultDelay: FiniteDuration)(implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    f recoverWith {
      case _ if retries > 0 =>
        after(delay.headOption.getOrElse(defaultDelay), s)(retry(f, delay.tail, retries - 1, defaultDelay))
    }
  }
}
