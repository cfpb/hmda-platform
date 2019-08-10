package hmda.dataBrowser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.dataBrowser.models.{Delimiter, QueryField}
import monix.eval.Task

trait FileService {
  def persistData(queries: List[QueryField],
                  delimiter: Delimiter,
                  data: Source[ByteString, NotUsed]): Task[Unit]

  def retrieveDataUrl(queries: List[QueryField],
                      delimiter: Delimiter): Task[Option[String]]

  def retrieveData(
      queries: List[QueryField],
      delimiter: Delimiter): Task[Option[Source[ByteString, NotUsed]]]
}
