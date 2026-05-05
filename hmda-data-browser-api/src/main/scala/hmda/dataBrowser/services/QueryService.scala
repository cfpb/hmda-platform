package hmda.dataBrowser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._

import monix.eval.Task

trait QueryService {
  def fetchAggregate(fields: QueryFields): Task[(ServedFrom, Seq[Aggregation])]
  def fetchData(fields: QueryFields): Source[ModifiedLarEntity, NotUsed]
  def fetchData2017(fields: QueryFields): Source[ModifiedLarEntity2017, NotUsed]
  def fetchFilers(fields: QueryFields): Task[(ServedFrom, FilerInstitutionResponseLatest)]
  def fetchFilers2017(fields: QueryFields): Task[(ServedFrom, FilerInstitutionResponse2017)]
}
