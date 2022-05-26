package hmda.dataBrowser.services

import akka.NotUsed
import akka.http.scaladsl.model.ContentTypes
import akka.stream.Materializer
import akka.stream.alpakka.s3.S3Headers
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString
import cats.implicits._
import hmda.dataBrowser.Settings
import hmda.dataBrowser.models.Delimiter.fileEnding
import hmda.dataBrowser.models.{ Delimiter, QueryField }
import monix.eval.Task
import org.slf4j.LoggerFactory

// $COVERAGE-OFF$
// All this does is use the Alpakka S3 APIs
class S3FileService(implicit mat: Materializer) extends FileService with Settings {

  private final val log = LoggerFactory.getLogger(getClass)

  override def persistData(queries: List[QueryField], delimiter: Delimiter, year: String, dataSource: Source[ByteString, NotUsed]): Task[Unit] = {
    val key = s3Key(queries, delimiter, year)
    val friendlyName = {
      val content = formName(queries)
      s"${content}${fileEnding(delimiter)}"
    }
    log.debug("Key: " + key)
    log.debug("Bucket: " + s3.bucket)
    // Content-Disposition is a friendly name that the user will see downloading the file
    // as opposed to the key which is an MD5 string
    // Note: don't use meta headers as it adds the x-amz- prefix to the header
    // Note: this is the correct format to set the content-disposition
    val contentDispositionMetadata =
    Map("Content-Disposition" -> s"attachment; filename=$friendlyName")
    val sink = S3.multipartUploadWithHeaders(
      bucket = s3.bucket,
      key = key,
      contentType = ContentTypes.`text/csv(UTF-8)`,
      s3Headers = S3Headers()
        .withCustomHeaders(contentDispositionMetadata)
    )

    Task.deferFuture {
      log.info("S3FileService Starting upload")
      dataSource.runWith(sink)
    }.onErrorHandleWith { error =>
      // Note: (this *> that) comes from using cats.implicits and it means execute `this` and discard results then run `that`
      log.error("S3FileService failure for key: {}, bucket: {}", key, s3.bucket, error)
      Task.eval(log.error(s"Failed to write data to the S3 bucket (Extended info: ${error.toString})", error)) *> Task
        .raiseError(error)
    }.void
  }

  override def retrieveData(queries: List[QueryField], delimiter: Delimiter, year: String): Task[Option[Source[ByteString, NotUsed]]] = {
    val key = s3Key(queries, delimiter, year)
    log.info("Retrieveing S3 Key: " + key)
    log.info("Retrieveing bucket name: " + s3.bucket)
    Task
      .deferFuture(S3.download(s3.bucket, key).runWith(Sink.head))
      .map(opt => opt.map { case (source, _) => source })
  }

  private def formName(queries: List[QueryField]): String = {
    // sort by name and then within each query field sort the values
    val name = queries
      .map(q => q.copy(values = q.values.sorted))
      .sortBy(_.dbName)
      .map(q => s"${q.name}_${q.values.mkString("-")}")
      .mkString("_")
    //If name is greater than 100 then slice to 100 and append md5 hash.
    //This is to prevent file names to grow over excel's 218 character limit while still maintaining unique file names for S3 Cache.
    name.length match {
      case x if x > 100 =>
        name.slice(0, 100) + md5HashString(name)
      case _ =>
        name
    }
  }

  private def s3Key(queries: List[QueryField], delimiter: Delimiter, year: String): String = {
    val input = md5HashString(formName(queries))
    println ("This is the input: " + input)
    val key = s"${s3.environment}/${s3.tableSelector(year.toInt)}/$input"
    s"$key${fileEnding(delimiter)}"
  }

  override def retrieveDataUrl(queries: List[QueryField], delimiter: Delimiter, year: String): Task[Option[String]] = {
    val key = s3Key(queries, delimiter, year)
    Task
      .deferFuture(S3.getObjectMetadata(s3.bucket, key).runWith(Sink.head))
      .onErrorHandleWith { error =>
        Task.eval(log.error(s"Failed to retrieve object metadata to the S3 bucket (Extended info: ${error.toString})", error)) *> Task
          .raiseError(error)
      }
      .map(opt => opt.map(_ => s"${s3.url}/$key"))
  }

  def healthCheck: Task[Unit] =
    Task.deferFuture(S3.checkIfBucketExists(s3.bucket)).void
}
// $COVERAGE-ON$