package hmda.reporting.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, encodeResponse, extractUri, get, handleRejections, onComplete, path}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.model.institution.{HmdaFiler, HmdaFilerResponse, MsaMd, MsaMdResponse}
import hmda.query.DbConfiguration.dbConfig
import hmda.reporting.query.ts.TsComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._
trait ReportingHttpApi extends TsComponent {
  import dbConfig.profile.api._
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  def tsRepository = new TransmittalSheetRepository(dbConfig)

  val filerListRoute: Route = {
    path("filers" / Segment) { year =>
      get {

        val filerSet = for {
          ts <- tsRepository.getAllSheets()
        } yield {
          ts.map(
            tsEntity =>
              HmdaFiler(tsEntity.lei,
                tsEntity.institutionName,
                tsEntity.year.toString))
            .toSet
        }

        onComplete(filerSet) {
          case Success(filerSet) =>
            complete(ToResponseMarshallable(HmdaFilerResponse(filerSet)))
          case Failure(error) =>
            complete(
              ToResponseMarshallable(
                StatusCodes.BadRequest -> error.getLocalizedMessage))
        }

      }
    } ~ path("filers" / Segment / Segment / "msaMds") { (year, lei) =>
      extractUri { uri =>
        val config: DatabaseConfig[JdbcProfile] = dbConfig
        println("This is the lei: " + lei)
        val resultset = for {
          myres1 <- config.db.run {
            sql"""select distinct msa_md, msa_md_name
                         from modifiedlar2018 where UPPER(lei) = ${lei.toUpperCase}"""
              .as[(String, String)]
          }
          institutionResult <- tsRepository.findByLei(lei)
        } yield {
          val myres = myres1
            .map(
              myEntity => MsaMd(myEntity._1, myEntity._2)
            )
            .toSet
          MsaMdResponse(new HmdaFiler(institutionResult.head.lei,
            institutionResult.head.name,
            institutionResult.head.year + ""),
            myres)
        }

        onComplete(resultset) {
          case Success(check) =>
            complete(ToResponseMarshallable(check))
          case Failure(error) =>
            val errorResponse =
              ErrorResponse(404, s"LEI not found", uri.path)
            complete(
              ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
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
