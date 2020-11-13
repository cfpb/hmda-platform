package hmda.publisher.helper

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

object RetryUtils {

  def retry[T](retries: Int, delay: Duration)(f: () => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    f() recoverWith { case _ if retries > 0 => Future(Thread.sleep(delay.toMillis)).flatMap(_ => retry(retries - 1, delay)(f)) }
  }

}