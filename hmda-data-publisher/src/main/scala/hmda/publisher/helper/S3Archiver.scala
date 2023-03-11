package hmda.publisher.helper
// $COVERAGE-OFF$
import java.time.Instant

import akka.stream.Materializer
import akka.stream.alpakka.s3.{ S3Attributes, S3Settings }
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ ExecutionContext, Future }

object S3Archiver extends LazyLogging{

  def archiveFileIfExists(srcBucket: String, srcKey: String, destBucket: String, s3Settings: S3Settings)(
    implicit ec: ExecutionContext,
    mat: Materializer
  ): Future[Unit] = {
    val extension = {
      val dotIdx = srcKey.lastIndexOf('.')
      if (dotIdx == -1) "" else srcKey.substring(dotIdx)
    }
    val destKey = srcKey.stripSuffix(extension) + s"_${Instant.now()}${extension}"
    copyIfExists(srcBucket, srcKey, destBucket, destKey, s3Settings)
  }

  private def copyIfExists(srcBucket: String, srcKey: String, destBucket: String, destKey: String, s3Settings: S3Settings)(
    implicit ec: ExecutionContext,
    mat: Materializer
  ): Future[Unit] =
    for {
      metadataOpt <- S3
        .getObjectMetadata(srcBucket, srcKey)
        .withAttributes(S3Attributes.settings(s3Settings))
        .runWith(Sink.head)
      srcExists = metadataOpt.isDefined
      _ <- if (srcExists) {
        logger.info("Archiving : " + destBucket + "/" + destKey)
        S3.multipartCopy(srcBucket, srcKey, destBucket, destKey)
          .withAttributes(S3Attributes.settings(s3Settings))
          .run()
      } else  {
        logger.info("ERROR ARCHIVING!!!")
        Future.unit}

    } yield ()
}
// $COVERAGE-ON$