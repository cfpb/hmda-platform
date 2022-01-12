package hmda.auth

import akka.actor.typed.ActorSystem
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.circe.parser.decode
import org.keycloak.adapters.KeycloakDeployment
import org.keycloak.representations.AccessToken
import java.security.KeyFactory
import org.keycloak.jose.jws.AlgorithmType
import org.keycloak.adapters.KeycloakDeployment
import java.math.BigInteger
import java.security.spec.RSAPublicKeySpec
import java.security.KeyFactory
import java.util.Base64
import org.keycloak.TokenVerifier

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

// $COVERAGE-OFF$
class KeycloakTokenVerifier(keycloakDeployment: KeycloakDeployment)(
  implicit system: ActorSystem[_],
  materializer: Materializer,
  ec: ExecutionContext
) extends TokenVerifier {

  val config  = ConfigFactory.load()
  val realm   = config.getString("keycloak.realm")
  val authUrl = config.getString("keycloak.auth.server.url")
  val timeout = config.getInt("hmda.http.timeout").seconds
  
  val keyFactory = KeyFactory.getInstance(AlgorithmType.RSA.toString)
  val urlDecoder = Base64.getUrlDecoder
  val modulus = new BigInteger(1, urlDecoder.decode(config.getString("keycloak.publicKey.modulus")))
  val publicExponent = new BigInteger(1, urlDecoder.decode(config.getString("keycloak.publicKey.exponent")))
  val publicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, publicExponent))

  def verifyToken(token: String): AccessToken = {
    val tokenVerifier = TokenVerifier.create(token, classOf[AccessToken])
      .withDefaultChecks()
    tokenVerifier.publicKey(publicKey).verify().getToken
  }

  private def parseAuthKey(line: ByteString): AuthKey = {
    val str      = line.utf8String
    val authKeys = decode[AuthKeys](str).getOrElse(AuthKeys())
    if (authKeys.keys.nonEmpty) authKeys.keys.head
    else AuthKey()
  }
}
// $COVERAGE-ON$