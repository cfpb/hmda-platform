package hmda.publisher.qa

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.s3.S3Attributes
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{ Framing, Source }
import akka.util.ByteString
import cats.instances.future._
import cats.syntax.all._
import com.typesafe.scalalogging.StrictLogging
import hmda.publisher.util.MattermostNotifier

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

class QAFilePersistor(notifier: MattermostNotifier)(implicit ec: ExecutionContext, materializer: Materializer) extends StrictLogging {

  private val persistBatchSize   = 100
  private val persistParallelism = 5

  def fetchAndPersist[T](spec: QAFileSpec[T]): Future[Unit] = {
    val timeStamp          = System.currentTimeMillis()

    logger.debug(s"Fetching and saving file ${spec.filePath} for QA")
    fetchFile(spec)
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = Int.MaxValue, allowTruncation = true))
      .map(_.utf8String)
      .drop(if (spec.withHeaderLine) 1 else 0)
      .map(line =>
        try {
          spec.parseLine(line)
        } catch {
          case e: Throwable =>
            logger.debug(s"Failed to parse line: $line", e)
            throw e
        }
      )
      .groupedWithin(persistBatchSize, 5.seconds)
      .mapAsync(persistParallelism)(batch => spec.repository.saveAll(batch, spec.filePath,timeStamp).map(_ => batch.size))
      .runFold(0)(_ + _)
      .flatTap(_ => spec.repository.deletePreviousRecords(timeStamp))
      .attempt
      .flatMap { result =>
        val msg = result match {
          case Left(exception) =>
            logger.error(s"Failed to load ${spec.filePath} into ${spec.repository.tableName}", exception)
            s"Failed to load ${spec.filePath} into ${spec.repository.tableName}: ${exception.getMessage}"
          case Right(count) =>
            logger.debug(s"${spec.filePath} loaded to ${spec.repository.tableName} with ${count} rows")
            s"${spec.filePath} loaded to ${spec.repository.tableName} with ${count} rows"
        }
        notifier.report(msg)
      }
  }

  private def fetchFile[T](s: QAFileSpec[T]): Source[ByteString, NotUsed] =
    S3.download(s.bucket, s.key)
      .withAttributes(S3Attributes.settings(s.s3Settings))
      .flatMapConcat({
        case Some(value) => value._1
        case None        => Source.failed(new Exception(s"S3 file ${s.filePath} not found"))
      })

}