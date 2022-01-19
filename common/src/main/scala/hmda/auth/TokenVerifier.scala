package hmda.auth

import org.keycloak.representations.AccessToken

import scala.concurrent.Future

import scala.util.Try

trait TokenVerifier {
  def verifyToken(token: String):Try[AccessToken]
}
