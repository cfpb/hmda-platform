package hmda.publisher.helper

import pekko.NotUsed
import pekko.stream.Materializer
import pekko.stream.alppekko.s3.MultipartUploadResult
import pekko.stream.scaladsl.{ Sink, Source }
import pekko.util.ByteString

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

object S3Utils {

  def uploadWithRetry(
                       source: Source[ByteString, NotUsed],
                       uploadSink: Sink[ByteString, Future[MultipartUploadResult]]
                     )(implicit mat: Materializer, ec: ExecutionContext): Future[MultipartUploadResult] =
    RetryUtils.retry(retries = 3, delay = 1.minute)(() => source.runWith(uploadSink))

}