package hmda.authService.api

import akka.NotUsed
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ Directive, Directive0 }
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.slf4j.Logger
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{ Sink, Source }

import scala.concurrent.Future
import akka.stream.alpakka.s3._
import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider
import software.amazon.awssdk.regions.providers._
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import scala.collection.JavaConverters._
import akka.actor.ActorSystem

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent._
import akka.http.scaladsl.model.ContentTypes

import scala.util.{ Failure, Success, Try }
import akka.http.scaladsl.model.StatusCodes.BadRequest
import hmda.auth.OAuth2Authorization
import akka.util.Timeout
import hmda.util.RealTimeConfig
import hmda.authService.model.UserUpdate
import hmda.institution.query.InstitutionEmailComponent
import hmda.api.http.model.ErrorResponse

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource._
import org.keycloak.representations.idm._

import scala.concurrent.duration._

object AuthHttpApi {
  def create(log: Logger)(implicit ec: ExecutionContext, system: ActorSystem): OAuth2Authorization => Route = new AuthHttpApi(log).authHttpRoutes _
}
private class AuthHttpApi(log: Logger)(implicit ec: ExecutionContext, system: ActorSystem) extends InstitutionEmailComponent{
    
    val config                  = ConfigFactory.load()
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("institution_db")

    val keycloakClientId        = config.getString("keycloak.client.id")
    val keycloakServerUrl       = config.getString("keycloak.auth.server.url")
    val keycloakRealm           = config.getString("keycloak.realm")
    val keycloakAdminUsername   = config.getString("keycloak.admin.username")
    val keycloakAdminPassword   = config.getString("keycloak.admin.password")

    implicit val institutionEmailsRepository: InstitutionEmailsRepository = new InstitutionEmailsRepository(dbConfig)

    val keycloak = KeycloakBuilder.builder()
                .serverUrl("https://hmda4.demo.cfpb.gov/auth/")
                .realm("master")
                .clientId("admin-cli")
                .grantType("password")
                .username("keycloak")
                .password("keycloak")
                .build()
    
    def authHttpRoutes(oAuth2Authorization: OAuth2Authorization): Route = {
        encodeResponse {
            pathPrefix("auth") {
                pathPrefix("user") {
                    (extractUri & put) { uri =>
                        oAuth2Authorization.authorizeVerifiedToken() { token =>
                            entity(as[UserUpdate]){ userUpdate =>
                                val allUsers: UsersResource = keycloak.realm("hmda2").users()
                                val userResource: UserResource = allUsers.get(token.id)
                                val user: UserRepresentation = userResource.toRepresentation()
                                val fIsValidLeis = verifyLeis(getDomainFromEmail(token.email), userUpdate.leis)
                                fIsValidLeis.onComplete {
                                    case Success(isValidLeis) =>
                                        if (isValidLeis) user.setAttributes(Map(("lei", List(userUpdate.leis.mkString(",")).asJava)).asJava)
                                        else complete((BadRequest, ErrorResponse(BadRequest.intValue, "LEIs are not authorized for the users email domain", uri.path)))
                                    case Failure(e) => 
                                        complete((BadRequest, ErrorResponse(StatusCodes.InternalServerError.intValue, "Failed to fetch list of authorized LEIs for users email domain", uri.path)))
                                }
                                if (userUpdate.firstName != user.getFirstName()) user.setFirstName(userUpdate.firstName)
                                if (userUpdate.lastName != user.getLastName()) user.setLastName(userUpdate.lastName)
                                println(user.getAttributes())
                                userResource.update(user)
                                complete(HttpResponse(StatusCodes.OK, entity = token.email))
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
            val oldLeis = institutions.map(institution => institution.LEI)
            oldLeis.toSet == proposedLeis.toSet
        }
    }
}
