package hmda.dataBrowser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.dataBrowser.models.{ModifiedLarEntity, QueryField}
import monix.eval.Task

sealed trait Delimiter

case object Comma extends Delimiter

case object Pipe extends Delimiter

trait FileService {
  def persistData(queries: List[QueryField],
                  delimiter: Delimiter,
                  data: Source[ModifiedLarEntity, NotUsed]): Task[Unit]

  def retrieveData(
                    queries: List[QueryField],
                    delimiter: Delimiter): Task[Option[Source[ByteString, NotUsed]]]
}