package hmda.api.http.directives

import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.{ BadRequest, Forbidden }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence
import org.slf4j.Logger

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object QuarterlyFilingAuthorization {
  def quarterlyFilingAllowed(log: Logger, sharding: ClusterSharding)(lei: String, year: Int)(
    successful: Route
  )(implicit timeout: Timeout): Route = {
    val institution                           = InstitutionPersistence.selectInstitution(sharding, lei, year)
    val response: Future[Option[Institution]] = institution ? GetInstitution
    extractMatchedPath { path =>
      onComplete(response) {
        case Failure(exception) =>
          log.error("Failed to retrieve institution when trying to check if institution can do quarterly filing", exception)
          complete(StatusCodes.InternalServerError)

        case Success(None) =>
          log.info(s"institution does not exist for LEI: $lei and year: $year")
          complete((BadRequest, ErrorResponse(BadRequest.intValue, "institution does not exist", path)))

        case Success(Some(i)) if i.quarterlyFiler =>
          successful

        case Success(Some(i)) =>
          log.info(s"institution for LEI: $lei and year: $year does not have permissions to do quarterly filing")
          complete((Forbidden, ErrorResponse(BadRequest.intValue, "Institution is not permitted to do quarterly filing", path)))
      }
    }
  }
}