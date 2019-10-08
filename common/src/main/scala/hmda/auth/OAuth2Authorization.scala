package hmda.auth

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import akka.http.scaladsl.server.Directives.{ reject, _ }
import akka.http.scaladsl.server._
import com.typesafe.config.ConfigFactory

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

  def authorizeTokenWithLei(lei: String): Directive1[VerifiedToken] =
    authorizeToken flatMap {
      case t if t.lei.nonEmpty =>
        if (runtimeMode == "dev") {
          provide(t)
        } else {
          val leiList = t.lei.split(',')
          if (leiList.contains(lei)) {
            provide(t)
          } else {
            reject(AuthorizationFailedRejection)
              .toDirective[Tuple1[VerifiedToken]]
          }
        }

      case _ =>
        if (runtimeMode == "dev") {
          provide(VerifiedToken())
        } else {
          reject(AuthorizationFailedRejection)
            .toDirective[Tuple1[VerifiedToken]]
        }
    }

  def authorizeToken: Directive1[VerifiedToken] =
    bearerToken.flatMap {
      case Some(token) =>
        onComplete(tokenVerifier.verifyToken(token)).flatMap {
          _.map { t =>
            val lei: String = if (t.getOtherClaims.containsKey("lei")) {
              t.getOtherClaims.get("lei").toString
            } else ""

            provide(
              VerifiedToken(token,
                            t.getId,
                            t.getName,
                            t.getPreferredUsername,
                            t.getEmail,
                            t.getResourceAccess().get(clientId).getRoles.asScala.toSeq,
                            lei)
            )
          }.recover {
            case ex: Throwable =>
              logger.error("Authorization Token could not be verified {}", ex)
              reject(AuthorizationFailedRejection)
                .toDirective[Tuple1[VerifiedToken]]
          }.get
        }
      case None =>
        if (runtimeMode == "dev") {
          provide(VerifiedToken())
        } else {
          reject(AuthorizationFailedRejection)
        }
    }

  private def bearerToken: Directive1[Option[String]] =
    for {
      authBearerHeader <- optionalHeaderValueByType(classOf[Authorization])
                           .map(extractBearerToken)
      xAuthCookie <- optionalCookie("X-Authorization-Token").map(_.map(_.value))
    } yield authBearerHeader.orElse(xAuthCookie)

  private def extractBearerToken(authHeader: Option[Authorization]): Option[String] =
    authHeader.collect {
      case Authorization(OAuth2BearerToken(token)) => token
    }

}

object OAuth2Authorization {
  def apply(logger: LoggingAdapter, tokenVerifier: TokenVerifier): OAuth2Authorization =
    new OAuth2Authorization(logger, tokenVerifier)
}
