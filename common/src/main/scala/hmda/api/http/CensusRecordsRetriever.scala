package hmda.publication.lar.services

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Keep, Sink, Source }
import hmda.model.census.Census
import scala.concurrent.{ ExecutionContext, Future }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.query.dtos.IndexedCensusEntry

sealed trait CensusType
case object Tract       extends CensusType
case object County      extends CensusType
case object SmallCounty extends CensusType

object CensusType {
  implicit class CensusTypeOps(censusType: CensusType) {
    def render: String = censusType match {
      case Tract       => "tract"
      case County      => "county"
      case SmallCounty => "smallcounty"
    }
  }
}

case class IndexedCensusMaps(tract: Map[String, Census], county: Map[String, Census], smallCounty: Map[String, Census])

class CensusRecordsRetriever(httpClient: HttpExt, censusApiUrl: String) {
  private implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport
      .json()
      .withParallelMarshalling(parallelism = 4, unordered = true)

  def downloadCensusMap(dataType: CensusType)(implicit mat: ActorMaterializer, ec: ExecutionContext): Future[Map[String, Census]] = {
    println(censusApiUrl + s"/streaming/${dataType.render}")
    httpClient
      .singleRequest(HttpRequest(uri = censusApiUrl + s"/streaming/${dataType.render}"))
      .flatMap { response =>
        val responseWithoutSizeLimit =
          response.copy(entity = response.entity.withoutSizeLimit())
        Unmarshal(responseWithoutSizeLimit)
          .to[Source[IndexedCensusEntry, NotUsed]]
      }
      .flatMap(
        indexedCensusStream =>
          indexedCensusStream
            .toMat(Sink.fold(Map.empty[String, Census]) { (acc, next: IndexedCensusEntry) =>
              acc + (next.index -> next.data)
            })(Keep.right)
            .run()
      )
  }

}

object CensusRecordsRetriever {
  def apply(httpClient: HttpExt, censusApiUrl: String): CensusRecordsRetriever =
    new CensusRecordsRetriever(httpClient, censusApiUrl)
}
