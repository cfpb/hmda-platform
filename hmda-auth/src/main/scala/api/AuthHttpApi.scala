package hmda.authService.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.slf4j.Logger

import com.typesafe.config.ConfigFactory

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.Future
import scala.util.{ Failure, Success}

import hmda.auth.OAuth2Authorization
import hmda.authService.model.UserUpdate
import hmda.api.http.model.ErrorResponse
import hmda.institution.query.InstitutionEmailComponent

import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource._
import org.keycloak.representations.idm._


object AuthHttpApi {
  def create(log: Logger)(implicit ec: ExecutionContext): OAuth2Authorization => Route = new AuthHttpApi(log).authHttpRoutes _
}
private class AuthHttpApi(log: Logger)(implicit ec: ExecutionContext) extends InstitutionEmailComponent{
    
    val config                  = ConfigFactory.load()
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("institution_db")

    val keycloakServerUrl       = config.getString("keycloak.auth.server.url")
    val keycloakRealm           = config.getString("keycloak.realm")
    val keycloakAdminUsername   = config.getString("keycloak.admin.username")
    val keycloakAdminPassword   = config.getString("keycloak.admin.password")

    implicit val institutionEmailsRepository: InstitutionEmailsRepository = new InstitutionEmailsRepository(dbConfig)
    
    val keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .grantType("password")
                .username(keycloakAdminUsername)
                .password(keycloakAdminPassword)
                .build()
    
    def authHttpRoutes(oAuth2Authorization: OAuth2Authorization): Route = {
        encodeResponse {
            pathPrefix("user") {
                (extractUri & put) { uri =>
                    oAuth2Authorization.authorizeVerifiedToken() { token =>
                        entity(as[UserUpdate]){ userUpdate =>
                            val allUsers: UsersResource = keycloak.realm(keycloakRealm).users()
                            val userResource: UserResource = allUsers.get(token.userId)
                            val user: UserRepresentation = userResource.toRepresentation()
                            val fIsValidLeis = verifyLeis(getDomainFromEmail(token.email), userUpdate.leis)
                            if (userUpdate.firstName != user.getFirstName()) user.setFirstName(userUpdate.firstName)
                            if (userUpdate.lastName != user.getLastName()) user.setLastName(userUpdate.lastName)
                            onComplete(fIsValidLeis) {
                                case Success(isValidLeis) =>
                                    if (isValidLeis) {
                                        user.setAttributes(Map(("lei", List(userUpdate.leis.mkString(",")).asJava)).asJava)
                                        userResource.update(user)
                                        complete(ToResponseMarshallable(userUpdate))
                                    }
                                    else complete((BadRequest, ErrorResponse(BadRequest.intValue, "LEIs are not authorized for the users email domain", uri.path)))
                                case Failure(e) => 
                                    complete((BadRequest, ErrorResponse(StatusCodes.InternalServerError.intValue, "Failed to fetch list of authorized LEIs for users email domain", uri.path)))
                            }
                        }
                    }
                }
            }
        }
    }

    private def getDomainFromEmail(email: String): String = email.split("@")(1)

    private def verifyLeis(emailDomain: String, proposedLeis: List[String]): Future[Boolean] = {
        findByEmailAnyYear(emailDomain).map { institutions =>
            val availableLeis = institutions.map(institution => institution.LEI)
            proposedLeis.forall(availableLeis.contains)
        }
    }
}
