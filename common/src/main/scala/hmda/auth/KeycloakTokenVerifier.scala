package hmda.auth

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.circe.parser.decode
import org.keycloak.RSATokenVerifier
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.representations.AccessToken

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class KeycloakTokenVerifier(keycloakDeployment: KeycloakDeployment)(
  implicit system: ActorSystem[_],
  materializer: Materializer,
  ec: ExecutionContext
) extends TokenVerifier {

  val config  = ConfigFactory.load()
  val realm   = config.getString("keycloak.realm")
  val authUrl = config.getString("keycloak.auth.server.url")
  val timeout = config.getInt("hmda.http.timeout").seconds

  override def verifyToken(token: String): Future[AccessToken] = {
    val fKid = getKid(keycloakDeployment)
    fKid.map { kid =>
      RSATokenVerifier.verifyToken(
        token,
        keycloakDeployment.getPublicKeyLocator.getPublicKey(kid, keycloakDeployment),
        keycloakDeployment.getRealmInfoUrl
      )
    }
  }

  private def getKid(keycloakDeployment: KeycloakDeployment): Future[String] = {
    val certUrl =
      s"${authUrl}realms/$realm/protocol/openid-connect/certs"
    val fResponse     = Http()(system.toClassic).singleRequest(HttpRequest(uri = certUrl))
    val fStrictEntity = fResponse.map(response => response.entity.toStrict(timeout))
    val f = for {
      _ <- fResponse
      s <- fStrictEntity
      e <- s.map(_.dataBytes)
      r = e
        .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
        .map(parseAuthKey)
    } yield r
    f.flatMap(a => a.map(_.kid))
  }

  private def parseAuthKey(line: ByteString): AuthKey = {
    val str      = line.utf8String
    val authKeys = decode[AuthKeys](str).getOrElse(AuthKeys())
    if (authKeys.keys.nonEmpty) authKeys.keys.head
    else AuthKey()
  }
}