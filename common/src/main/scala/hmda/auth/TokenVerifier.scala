package hmda.auth

import org.keycloak.representations.AccessToken

import scala.concurrent.Future

trait TokenVerifier {
  def verifyToken(token: String): Future[AccessToken]
}
