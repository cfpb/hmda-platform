package hmda.auth

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{AttributeKey, HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LoggingMagnet
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.representations.adapters.config.AdapterConfig
import org.slf4j.Logger
import org.keycloak.common.crypto.CryptoIntegration

import java.util.concurrent.atomic.AtomicReference
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

// $COVERAGE-OFF$
class OAuth2Authorization(logger: Logger, tokenVerifier: TokenVerifier) {
  CryptoIntegration.init(this.getClass.getClassLoader)

  private val tokenAttributeRefKey = AttributeKey[AtomicReference[VerifiedToken]]("tokenRef")

  val config      = ConfigFactory.load()
  val clientId    = config.getString("keycloak.client.id")
  val runtimeMode = config.getString("hmda.runtime.mode")

  def authorizeTokenWithLeiOrRole(lei: String, role: String): Directive1[VerifiedToken] =
    withAccessLog
      .&(handleRejections(authRejectionHandler))
      .&(authorizeTokenWithLeiReject(lei.trim()) | authorizeTokenWithRoleReject(role))

  def authorizeTokenWithLei(lei: String): Directive1[VerifiedToken] =
    withAccessLog
      .&(handleRejections(authRejectionHandler))
      .&(authorizeTokenWithLeiReject(lei.trim()))

  def authorizeTokenWithRole(role: String): Directive1[VerifiedToken] =
    withAccessLog
      .&(handleRejections(authRejectionHandler))
      .&(authorizeTokenWithRoleReject(role))

  def authorizeVerifiedToken(): Directive1[VerifiedToken] =
    withAccessLog
      .&(handleRejections(authRejectionHandler))
      .&(passToken)

  def logAccessLog(uri: Uri, token: () => Option[VerifiedToken])(request: HttpRequest)(r: RouteResult): Unit = {
    val result = r match {
      case RouteResult.Complete(response)   => s"completed(${response.status.intValue()})"
      case RouteResult.Rejected(_) => s"rejected"
    }
    logger.debug(s"""Access attempt:
                    |uri = ${uri}
                    |username = ${token().map(_.username).getOrElse("unknown")}
                    |result = ${result}""".stripMargin)
  }

  def withAccessLog: Directive[Unit] = {
    // this is a hack, but a simplest way to save the token down the road and read it on the way back
    val ref = new AtomicReference[VerifiedToken]()
    (extractUri & mapRequest(_.addAttribute(tokenAttributeRefKey, ref))).flatMap((uri =>
      logRequestResult(LoggingMagnet(_ => logAccessLog(uri, () => Option(ref.get()))))))
  }

  protected def authorizeTokenWithRoleReject(role: String): Directive1[VerifiedToken] =
    authorizeToken flatMap {
      case t if t.roles.contains(role) =>
        provide(t)
      case _ =>
        withLocalModeBypass {
          reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
        }
    }

  protected def passToken(): Directive1[VerifiedToken] = 
    authorizeToken flatMap {
      case t =>
        provide(t)
      case _ =>
         withLocalModeBypass {
          reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
        }
    }

  protected def authRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle({
        case AuthorizationFailedRejection =>
          complete(
            (StatusCodes.Forbidden, ErrorResponse(StatusCodes.Forbidden.intValue, "Authorization Token could not be verified", Path("")))
          )
      })
      .result()

  protected def authorizeTokenWithLeiReject(lei: String): Directive1[VerifiedToken] =
    authorizeToken flatMap {
      case t if t.lei.nonEmpty =>
        withLocalModeBypass {
          val leiList = t.lei.split(',').map(_.trim())
          if (leiList.contains(lei.trim())) {
            provide(t)
          } else {
            logger.info(s"Providing reject for ${lei.trim()}")
            reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
          }
        }

      case _ =>
        withLocalModeBypass {
          logger.info("Rejecting request in authorizeTokenWithLei")
          reject(AuthorizationFailedRejection).toDirective[Tuple1[VerifiedToken]]
        }
    }

  protected def withLocalModeBypass(thunk: => Directive1[VerifiedToken]): Directive1[VerifiedToken] =
    if (runtimeMode == "dev" || runtimeMode == "docker-compose" || runtimeMode == "kind") {
      provide(VerifiedToken())
    } else { thunk }

  protected def authorizeToken: Directive1[VerifiedToken] =
    bearerToken.flatMap {
      case Some(token) =>
        val t = tokenVerifier.verifyToken(token)
        t match {
          case Success(vT) => {
            val lei: String = vT.getOtherClaims.asScala
            .get("lei")
            .map(_.toString)
            .getOrElse("")
            val verifiedToken = VerifiedToken(
              token,
              vT.getId,
              vT.getSubject,
              vT.getName,
              vT.getPreferredUsername,
              vT.getEmail,
              vT.getResourceAccess().get(clientId).getRoles.asScala.toSeq,
              lei
            )
            attribute(tokenAttributeRefKey).flatMap(tokenRef => {
              tokenRef.set(verifiedToken)
              provide(verifiedToken)
            })
          }
        case Failure(e) =>
          withLocalModeBypass {
            val r: Route = (extractRequest { req =>
              reject(AuthorizationFailedRejection)
            })
            logger.error("Token could not be verified: " + e)
            StandardRoute(r).toDirective[Tuple1[VerifiedToken]]
          }
        }
        
      case None =>
        withLocalModeBypass {
          val r: Route = (extractRequest { req =>
            import scala.compat.java8.OptionConverters._
            logger.error("No bearer token, auth header [{}]" + req.getHeader("authorization").asScala)
            reject(AuthorizationFailedRejection)
          })
          StandardRoute(r).toDirective[Tuple1[VerifiedToken]]
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

  def apply(log: Logger, config: Config): OAuth2Authorization = {
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