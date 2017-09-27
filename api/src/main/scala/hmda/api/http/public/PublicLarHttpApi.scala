package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import hmda.api.EC
import hmda.api.http.HmdaCustomDirectives

trait PublicLarHttpApi extends HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  //val modifiedLarRepository = new ModifiedLarRepository(config)

  def modifiedLar[_: EC](institutionId: String) =
    path("filings" / Segment / "lar") { period =>
      timedGet { _ =>
        //TODO: reimplement modified lar endpoint when Cassandra backend implementation is done
        //val data = modifiedLarRepository.findByInstitutionIdSource(institutionId, period)
        //  .map(x => ChunkStreamPart(x.toCSV + "\n"))
        //val response = HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/csv(UTF-8)`, data))
        //complete(response)
        complete("OK")
      }
    }

}
