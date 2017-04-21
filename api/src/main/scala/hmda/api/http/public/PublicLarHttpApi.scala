package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import hmda.api.EC
import hmda.api.http.HmdaCustomDirectives
import hmda.query.DbConfiguration._
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext

trait PublicLarHttpApi extends HmdaCustomDirectives with FilingComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val modifiedLarRepository = new ModifiedLarRepository(config)

  def modifiedLar[_: EC](institutionId: String) =
    path("filings" / Segment / "lar") { period =>
      timedGet { _ =>
        val data = modifiedLarRepository.findByInstitutionIdSource(institutionId, period)
          .map(x => ChunkStreamPart(x.toCSV + "\n"))
        val response = HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/csv(UTF-8)`, data))
        complete(response)
      }
    }

}
