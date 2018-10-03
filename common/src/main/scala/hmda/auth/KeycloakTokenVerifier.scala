package hmda.auth

import com.typesafe.config.ConfigFactory
import org.keycloak.RSATokenVerifier
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.representations.AccessToken

import scala.concurrent.{ExecutionContext, Future}

class KeycloakTokenVerifier(keycloakDeployment: KeycloakDeployment)(
    implicit ec: ExecutionContext)
    extends TokenVerifier {

  val config = ConfigFactory.load()

  val kid = config.getString("keycloak.public.key.id")

  override def verifyToken(token: String): Future[AccessToken] = {
    Future {
      RSATokenVerifier.verifyToken(
        token,
        keycloakDeployment.getPublicKeyLocator.getPublicKey(kid,
                                                            keycloakDeployment),
        keycloakDeployment.getRealmInfoUrl
      )
    }
  }
}
