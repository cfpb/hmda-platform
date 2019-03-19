package hmda.reporting.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{
  cors,
  corsRejectionHandler
}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.model.institution.{
  HmdaFiler,
  HmdaFilerResponse,
  MsaMd,
  MsaMdResponse
}
import hmda.query.repository.ModifiedLarRepository
import hmda.reporting.repository.TsComponent
import hmda.util.http.FilingResponseUtils.entityNotPresentResponse
import io.circe.generic.auto._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
trait ReportingHttpApi extends TsComponent {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  val tsRepository: TransmittalSheetRepository

  val filerListRoute: Route = {
    path("filers" / Segment) { year =>
      get {

        val futFilerSet =
          tsRepository
            .getAllSheets()
            .map(
              sheets =>
                sheets
                  .map(tsEntity =>
                    HmdaFiler(tsEntity.lei,
                              tsEntity.institutionName,
                              tsEntity.year.toString))
                  .toSet)
        onComplete(futFilerSet) {
          case Success(filerSet) =>
            complete(HmdaFilerResponse(filerSet))
          case Failure(error) =>
            complete(StatusCodes.BadRequest -> error.getLocalizedMessage)
        }

      }
    } ~ path("filers" / IntNumber / Segment / "msaMds") { (year, lei) =>
      extractUri { uri =>
        val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
        val repo = new ModifiedLarRepository("modifiedlar2018", databaseConfig)
        val resultset = for {
          msaMdsResult <- repo.msaMds(lei, year)
          institutionResult <- tsRepository.findByLei(lei)
        } yield {
          val msaMds =
            msaMdsResult.map(myEntity => MsaMd(myEntity._1, myEntity._2)).toSet
          MsaMdResponse(new HmdaFiler(institutionResult.head.lei,
                                      institutionResult.head.name,
                                      institutionResult.head.year + ""),
                        msaMds)
        }

        onComplete(resultset) {
          case Success(leiMsaMds) =>
            complete(leiMsaMds)
          case Failure(error) =>
            entityNotPresentResponse("institution", lei, uri)
        }
      }
    }

  }

  def hmdaFilerRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          filerListRoute
        }
      }
    }
  }
}
