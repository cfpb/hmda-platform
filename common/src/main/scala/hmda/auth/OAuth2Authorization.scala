package hmda.auth

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Directives.{reject, _}
import akka.http.scaladsl.server._
import com.typesafe.config.ConfigFactory
import hmda.api.http.model.ErrorResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import scala.collection.JavaConverters._

class OAuth2Authorization(logger: LoggingAdapter, tokenVerifier: TokenVerifier) {

  val config      = ConfigFactory.load()
  val clientId    = config.getString("keycloak.client.id")
  val runtimeMode = config.getString("hmda.runtime.mode")

  def authorizeTokenWithRole(role: String): Directive1[VerifiedToken] =
    authorizeToken flatMap {
      case t if t.roles.contains(role) =>
        provide(t)
      case _ =>
        if (runtimeMode == "dev") {
          provide(VerifiedToken())
        } else {
          reject(AuthorizationFailedRejection)
            .toDirective[Tuple1[VerifiedToken]]
        }
    }

  def authorizeTokenWithLei(lei: String): Directive1[VerifiedToken] = {
    logger.info(s"Inside authorizeTokenWithLei for ${lei}")
    authorizeToken flatMap {
      case t if t.lei.nonEmpty =>
        if (runtimeMode == "dev") {
          provide(t)
        } else {
          logger.info(s"Got token back for ${lei}")
          val leiList = t.lei.split(',')
          if (leiList.contains(lei)) {
            logger.info(s"Providing for ${lei}")
            provide(t)
          } else {
            logger.info(s"Providing reject for ${lei}")
            reject(AuthorizationFailedRejection)
              .toDirective[Tuple1[VerifiedToken]]
          }
        }

      case _ =>
        if (runtimeMode == "dev") {
          provide(VerifiedToken())
        } else {
          logger.error("Rejecting request in authorizeTokenWithLei")
          reject(AuthorizationFailedRejection)
            .toDirective[Tuple1[VerifiedToken]]
        }
    }
  }


  def authorizeToken: Directive1[VerifiedToken] = {
    logger.info("Inside authorizeToken")
    bearerToken.flatMap {
      case Some(token) =>
        onComplete(tokenVerifier.verifyToken(token)).flatMap {
          _.map { t =>
            val lei: String = if (t.getOtherClaims.containsKey("lei")) {
              t.getOtherClaims.get("lei").toString
            } else ""

            provide(
              VerifiedToken(
                token,
                t.getId,
                t.getName,
                t.getPreferredUsername,
                t.getEmail,
                t.getResourceAccess().get(clientId).getRoles.asScala.toSeq,
                lei
              )
            )
          }.recover {
            case ex: Throwable =>
              logger.error("Authorization Token could not be verified {}", ex)
//              reject(AuthorizationFailedRejection)
//                .toDirective[Tuple1[VerifiedToken]]
              complete(StatusCodes.Forbidden, ErrorResponse(StatusCodes.Forbidden.intValue, "Authorization Token could not be verified", Path(""))).toDirective[Tuple1[VerifiedToken]]
          }.get
        }
      case None =>
        if (runtimeMode == "dev") {
          provide(VerifiedToken())
        } else {
          logger.error("No bearer token found")
//          reject(AuthorizationFailedRejection)
          complete(StatusCodes.Forbidden, ErrorResponse(StatusCodes.Forbidden.intValue, "Authorization Token could not be verified", Path(""))).toDirective[Tuple1[VerifiedToken]]
        }
    }
  }

  private def bearerToken: Directive1[Option[String]] = {
    logger.info("Getting bearerToken")
    for {
      authBearerHeader <- optionalHeaderValueByType(classOf[Authorization])
                           .map(extractBearerToken)
      xAuthCookie <- optionalCookie("X-Authorization-Token").map(_.map(_.value))
    } yield authBearerHeader.orElse(xAuthCookie)
  }

  private def extractBearerToken(authHeader: Option[Authorization]): Option[String] =
    authHeader.collect {
      case Authorization(OAuth2BearerToken(token)) => token
    }

}

object OAuth2Authorization {
  def apply(logger: LoggingAdapter, tokenVerifier: TokenVerifier): OAuth2Authorization =
    new OAuth2Authorization(logger, tokenVerifier)
}
