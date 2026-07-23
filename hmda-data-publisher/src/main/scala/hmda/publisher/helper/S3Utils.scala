package hmda.publisher.helper

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.connectors.s3.MultipartUploadResult
import org.apache.pekko.stream.scaladsl.{ Sink, Source }
import org.apache.pekko.util.ByteString

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

object S3Utils {

  def uploadWithRetry(
                       source: Source[ByteString, NotUsed],
                       uploadSink: Sink[ByteString, Future[MultipartUploadResult]]
                     )(implicit mat: Materializer, ec: ExecutionContext): Future[MultipartUploadResult] =
    RetryUtils.retry(retries = 3, delay = 1.minute)(() => source.runWith(uploadSink))

}