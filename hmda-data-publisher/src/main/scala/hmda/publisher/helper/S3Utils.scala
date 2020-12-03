package hmda.publisher.helper

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

object S3Utils {

  def uploadWithRetry(
                       source: Source[ByteString, NotUsed],
                       uploadSink: Sink[ByteString, Future[MultipartUploadResult]]
                     )(implicit mat: Materializer, ec: ExecutionContext): Future[MultipartUploadResult] =
    RetryUtils.retry(retries = 3, delay = 1.minute)(() => source.runWith(uploadSink))

}