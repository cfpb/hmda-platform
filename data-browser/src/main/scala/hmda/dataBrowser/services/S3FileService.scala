package hmda.dataBrowser.services

import java.math.BigInteger
import java.security.MessageDigest

import akka.NotUsed
import akka.http.scaladsl.model.ContentTypes
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MetaHeaders, S3Headers}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import hmda.dataBrowser.Settings
import hmda.dataBrowser.models.{ModifiedLarEntity, QueryField}
import monix.eval.Task

class S3FileService(implicit mat: ActorMaterializer)
  extends FileService
    with Settings {
  private def serialize(source: Source[ModifiedLarEntity, NotUsed],
                        delimiter: Delimiter): Source[ByteString, NotUsed] = {
    source
      .map(e =>
        delimiter match {
          case Comma => s"${e.toCsv}\n"
          case Pipe  => s"${e.toPipe}\n"
        })
      .map(ByteString(_))
  }

  private def s3Key(queries: List[QueryField], delimiter: Delimiter): String = {
    def md5HashString(s: String): String = {
      val md = MessageDigest.getInstance("MD5")
      val digest = md.digest(s.getBytes)
      val bigInt = new BigInteger(1, digest)
      val hashedString = bigInt.toString(16)
      hashedString
    }

    def formName(queries: List[QueryField]): String = {
      // sort by name and then within each query field sort the values
      val sortedQueries =
        queries
          .map(q => q.copy(values = q.values.sorted))
          .sortBy(_.dbName)
          .map(q => s"${q.name}=${q.values.mkString("[", ",", "]")}")
          .mkString("|")
      md5HashString(sortedQueries)
    }

    val input = md5HashString(formName(queries))
    val key = s"${s3.environment}/${s3.filteredQueries}/$input"
    delimiter match {
      case Comma => s"$key.csv"
      case Pipe  => s"$key.psv"
    }
  }

  override def persistData(
                            queries: List[QueryField],
                            delimiter: Delimiter,
                            data: Source[ModifiedLarEntity, NotUsed]): Task[Unit] = {
    val key = s3Key(queries, delimiter)
    val metadata = Map("Content-Disposition" -> "attachment", "filename" -> key)
    val sink = S3.multipartUploadWithHeaders(
      bucket = s3.bucket,
      key = key,
      contentType = ContentTypes.`text/csv(UTF-8)`,
      s3Headers = S3Headers().withMetaHeaders(MetaHeaders(metadata))
    )
    Task
      .deferFuture {
        serialize(data, delimiter)
          .toMat(sink)(Keep.right)
          .run()
      }
      .map(_ => ())
  }

  override def retrieveData(
                             queries: List[QueryField],
                             delimiter: Delimiter): Task[Option[Source[ByteString, NotUsed]]] = {
    val key = s3Key(queries, delimiter)
    Task
      .deferFuture(S3.download(s3.bucket, key).runWith(Sink.head))
      .map(opt => opt.map { case (source, _) => source })
  }
}