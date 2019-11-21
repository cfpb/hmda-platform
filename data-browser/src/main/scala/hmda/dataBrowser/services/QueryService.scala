package hmda.dataBrowser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._

import monix.eval.Task

trait QueryService {
  def fetchAggregate(fields: List[QueryField]): Task[Seq[Aggregation]]
  def fetchData(fields: List[QueryField]): Source[ModifiedLarEntity, NotUsed]
  def fetchFilers(fields: List[QueryField]): Task[FilerInstitutionResponse]
}
