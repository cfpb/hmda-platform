//package hmda.api.http.admin
//
//import akka.actor.typed.ActorSystem
//import akka.actor.typed.scaladsl.adapter._
//import akka.cluster.sharding.typed.scaladsl.ClusterSharding
//import akka.cluster.typed.{Cluster, Join}
//import akka.event.{LoggingAdapter, NoLogging}
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.Route
//import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
//import akka.stream.ActorMaterializer
//import akka.testkit._
//import akka.util.Timeout
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//import hmda.api.http.model.admin.InstitutionDeletedResponse
//import hmda.auth.{KeycloakTokenVerifier, OAuth2Authorization}
//import hmda.model.institution.Institution
//import hmda.model.institution.InstitutionGenerators._
//import hmda.persistence.AkkaCassandraPersistenceSpec
//import hmda.persistence.institution.InstitutionPersistence
//import org.keycloak.adapters.KeycloakDeploymentBuilder
//import org.scalatest.MustMatchers
//
//import scala.concurrent.ExecutionContext
//import scala.concurrent.duration._
//import scala.util.Random
//
//class InstitutionAdminHttpApiSpec
//    extends AkkaCassandraPersistenceSpec
//    with MustMatchers
//    with InstitutionAdminHttpApi
//    with ScalatestRouteTest {
//
//  val duration = 10.seconds
//
//  implicit val routeTimeout = RouteTestTimeout(duration.dilated)
//
//  override implicit val typedSystem: ActorSystem[_] = system.toTyped
//  override implicit val materializer: ActorMaterializer = ActorMaterializer()
//  override val log: LoggingAdapter = NoLogging
//  override val ec: ExecutionContext = system.dispatcher
//  override implicit val timeout: Timeout = Timeout(duration)
//  override val sharding: ClusterSharding = ClusterSharding(typedSystem)
//
//  override def beforeAll(): Unit = {
//    super.beforeAll()
//    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
//    InstitutionPersistence.startShardRegion(sharding)
//  }
//
//  override def afterAll(): Unit = super.afterAll()
//
//  val lei = Random.alphanumeric.take(20).mkString.toUpperCase
//  val sampleInstitution =
//    institutionGen.sample
//      .getOrElse(Institution.empty)
//      .copy(LEI = lei)
//      .copy(taxId = Option("12-3456789"))
//      .copy(activityYear = 2018)
//
//  val modified =
//    sampleInstitution.copy(emailDomains = List("email@bank.com"))
//
//  val filerFlagsNegated = modified.copy(hmdaFiler = !modified.hmdaFiler,
//    quarterlyFilerHasFiledQ1 = !modified.quarterlyFilerHasFiledQ1,
//    quarterlyFilerHasFiledQ2 = !modified.quarterlyFilerHasFiledQ2,
//    quarterlyFilerHasFiledQ3 = !modified.quarterlyFilerHasFiledQ3
//  )
//  val oAuth2Authorization = OAuth2Authorization(
//    log,
//    new KeycloakTokenVerifier(
//      KeycloakDeploymentBuilder.build(
//        getClass.getResourceAsStream("/keycloak.json")
//      )
//    )
//  )
//
//  "Institutions HTTP Service" must {
//
//    "Create an institution" in {
//      Post("/institutions", sampleInstitution) ~> institutionAdminRoutes(
//        oAuth2Authorization) ~> check {
//        status mustBe StatusCodes.Created
//        responseAs[Institution] mustBe sampleInstitution
//      }
//    }
//
//    "Return a 400 on a duplicate LEI" in {
//      Post("/institutions", sampleInstitution) ~> Route.seal(
//        institutionAdminRoutes(oAuth2Authorization)) ~> check {
//        status mustBe StatusCodes.BadRequest
//      }
//    }
//
//    "Return a 404 on a wrongpath to create an institution" in {
//      Post("/wrongpath", sampleInstitution) ~> Route.seal(
//        institutionAdminRoutes(oAuth2Authorization)) ~> check {
//        status mustBe StatusCodes.NotFound
//      }
//    }
//
//    "Return a 404 on a wrongpath" in {
//      Post("/wrongpath") ~> Route.seal(
//        institutionAdminRoutes(oAuth2Authorization)) ~> check {
//        status mustBe StatusCodes.NotFound
//      }
//    }
//
//    "Get an institution" in {
//      Get(s"/institutions/${sampleInstitution.LEI}/year/2018") ~> institutionAdminRoutes(
//        oAuth2Authorization) ~> check {
//        status mustBe StatusCodes.OK
//        responseAs[Institution] mustBe sampleInstitution
//      }
//    }
//
//    "Return a 404 on a wrongpath to get an institution " in {
//      Get(s"/wrongpath/${sampleInstitution.LEI}/year/2018") ~> Route.seal(
//        institutionAdminRoutes(oAuth2Authorization)) ~> check {
//        status mustBe StatusCodes.NotFound
//      }
//    }
//
//    "Modify an institution" in {
//      Put("/institutions", modified) ~> institutionAdminRoutes(
//        oAuth2Authorization) ~> check {
//        status mustBe StatusCodes.Accepted
//        responseAs[Institution] mustBe modified
//      }
//    }
//
//    "Return a 404 on a wrongpath to modify an institution" in {
//      Put("/wrongpath", modified) ~> Route.seal(
//        institutionAdminRoutes(oAuth2Authorization)) ~> check {
//        status mustBe StatusCodes.NotFound
//      }
//    }
//
//    "Ignore filer flag for an institution with filer flag set" in {
//      Put("/institutions", filerFlagsNegated) ~> institutionAdminRoutes(
//        oAuth2Authorization) ~> check {
//        status mustBe StatusCodes.Accepted
//        responseAs[Institution] mustBe modified
//      }
//    }
//
//    "Delete an institution" in {
//      Delete("/institutions", modified) ~> institutionAdminRoutes(
//        oAuth2Authorization) ~> check {
//        status mustBe StatusCodes.Accepted
//        responseAs[InstitutionDeletedResponse] mustBe InstitutionDeletedResponse(
//          lei)
//      }
//    }
//
//    "Return a 404 on a wrongpath to delete an institution" in {
//      Delete("/institutions", modified) ~> Route.seal(
//        institutionAdminRoutes(oAuth2Authorization)) ~> check {
//        status mustBe StatusCodes.NotFound
//      }
//    }
//
//  }
//
//}