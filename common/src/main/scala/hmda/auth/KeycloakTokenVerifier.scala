package hmda.auth

import com.typesafe.config.ConfigFactory
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.representations.AccessToken
import java.security.KeyFactory
import org.keycloak.jose.jws.AlgorithmType
import java.math.BigInteger
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import org.keycloak.{TokenVerifier => keycloakTV}
import scala.util.Try
import scala.concurrent.duration._

// $COVERAGE-OFF$
class KeycloakTokenVerifier(keycloakDeployment: KeycloakDeployment) extends TokenVerifier {

  val config  = ConfigFactory.load()
  val realm   = config.getString("keycloak.realm")
  val realmUrl = config.getString("keycloak.realmUrl")
  val authUrl = config.getString("keycloak.auth.server.url")
  val timeout = config.getInt("hmda.http.timeout").seconds
  
  val keyFactory = KeyFactory.getInstance(AlgorithmType.RSA.toString)
  val urlDecoder = Base64.getUrlDecoder
  val modulus = new BigInteger(1, urlDecoder.decode(config.getString("keycloak.publicKey.modulus")))
  val publicExponent = new BigInteger(1, urlDecoder.decode(config.getString("keycloak.publicKey.exponent")))
  lazy val publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent))

  def verifyToken(token: String): Try[AccessToken] = {
    val tokenVerifier = keycloakTV.create(token, classOf[AccessToken])
    Try {
      tokenVerifier.withDefaultChecks().realmUrl(realmUrl)
      tokenVerifier.publicKey(publicKey).verify().getToken
    }
  }
}
// $COVERAGE-ON$