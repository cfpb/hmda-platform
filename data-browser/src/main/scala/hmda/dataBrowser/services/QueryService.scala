package hmda.dataBrowser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._

import monix.eval.Task

trait QueryService {
  def fetchAggregate(fields: QueryFields): Task[Seq[Aggregation]]
  def fetchData(fields: QueryFields): Source[ModifiedLarEntity, NotUsed]
  def fetchFilers(fields: QueryFields): Task[FilerInstitutionResponse]
}
