package hmda.auth

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.keycloak.representations.AccessToken

import scala.concurrent.Future

trait TokenVerifier {
  def verifyToken(token: String): Future[AccessToken]
}
