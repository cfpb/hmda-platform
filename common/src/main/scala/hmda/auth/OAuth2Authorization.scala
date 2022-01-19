package hmda.auth

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{AttributeKey, HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.representations.adapters.config.AdapterConfig
import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
// $COVERAGE-OFF$
class OAuth2Authorization(logger: Logger, tokenVerifier: TokenVerifier) {

  val config      = ConfigFactory.load()
  val clientId    = config.getString("keycloak.client.id")
  val runtimeMode = config.getString("hmda.runtime.mode")

  def authorizeTokenWithRule(authRule: AuthRule, comparator: String = ""): Directive1[VerifiedToken] = {
    withLocalModeBypass {
      authorizeToken flatMap {
        case token =>
          withAccessLog(token)
            .&(handleRejections(authRejectionHandler(authRule.rejectMessage)))
            .&(authorizeTokenWithRuleReject(authRule.rule(token, comparator), token))
        case _ =>
            reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
      }
    }
  }

  def logAccessLog(uri: Uri, token: VerifiedToken)(request: HttpRequest)(r: RouteResult): Unit = {
    val result = r match {
      case RouteResult.Complete(response)   => s"completed(${response.status.intValue()})"
      case RouteResult.Rejected(_) => s"rejected"
    }
    logger.debug(s"""Access attempt:
                    |uri = ${uri}
                    |username = ${token.username}
                    |result = ${result}""".stripMargin)
  }

  def withAccessLog(token: VerifiedToken): Directive[Unit] = {
    (extractUri).flatMap((uri =>
      logRequestResult(LoggingMagnet(_ => logAccessLog(uri, token)))))
  }

  protected def authorizeTokenWithRuleReject(passing: Boolean, token: VerifiedToken): Directive1[VerifiedToken] = {
        if (passing) provide(token)
        else reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
  }

  protected def authRejectionHandler(rejectionMessage: String): RejectionHandler = {
    println("authRejectioHandler")
    RejectionHandler
      .newBuilder()
      .handle({
        case AuthorizationFailedRejection =>
          println("providing message")
          complete(
            (StatusCodes.Forbidden, ErrorResponse(StatusCodes.Forbidden.intValue, rejectionMessage, Path("")))
          )
      })
      .result()
  }

  protected def withLocalModeBypass(thunk: => Directive1[VerifiedToken]): Directive1[VerifiedToken] =
    if (runtimeMode == "dev" || runtimeMode == "docker-compose" || runtimeMode == "kind") {
      provide(VerifiedToken())
    } else { thunk }

  protected def authorizeToken: Directive1[VerifiedToken] = {
    bearerToken.flatMap {
      case Some(token) =>
        onComplete(tokenVerifier.verifyToken(token)).flatMap {
          _.map { t =>
            val lei: String = t.getOtherClaims.asScala
              .get("lei")
              .map(_.toString)
              .getOrElse("")
            val verifiedToken = VerifiedToken(
              token,
              t.getId,
              t.getName,
              t.getPreferredUsername,
              t.getEmail,
              t.getResourceAccess().get(clientId).getRoles.asScala.toSeq,
              lei
            )
            provide(verifiedToken)
          }.recover {
            case ex: Throwable =>
              logger.error("Authorization Token could not be verified", ex)
              reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
          }.get
        }
      case None =>
        withLocalModeBypass {
          val r: Route = (extractRequest { req =>
            import scala.compat.java8.OptionConverters._
            logger.error("No bearer token, authz header [{}]" + req.getHeader("authorization").asScala)
            reject(AuthorizationFailedRejection)
          })
          StandardRoute(r).toDirective[Tuple1[VerifiedToken]]
        }
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

  def apply(log: Logger, config: Config)(implicit system: ActorSystem[_], mat: Materializer, ec: ExecutionContext): OAuth2Authorization = {
    val authUrl       = config.getString("keycloak.auth.server.url")
    val keycloakRealm = config.getString("keycloak.realm")
    val apiClientId   = config.getString("keycloak.client.id")
    val adapterConfig = new AdapterConfig()
    adapterConfig.setRealm(keycloakRealm)
    adapterConfig.setAuthServerUrl(authUrl)
    adapterConfig.setResource(apiClientId)
    val keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig)
    OAuth2Authorization(log, new KeycloakTokenVerifier(keycloakDeployment))
  }

  def apply(logger: Logger, tokenVerifier: TokenVerifier): OAuth2Authorization =
    new OAuth2Authorization(logger, tokenVerifier)
}
// This is just a Guardian for starting up the API
// $COVERAGE-OFF$