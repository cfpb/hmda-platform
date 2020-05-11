package hmda.api.http.directives

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionEvent }
import hmda.model.institution.Institution
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institution.InstitutionPersistence
import org.scalatest.{ Assertion, MustMatchers }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.duration._

class QuarterlyFilingAuthorizationSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest {

  override implicit val typedSystem: ActorSystem[_] = system.toTyped
  val sharding                                      = ClusterSharding(typedSystem)
  implicit val timeout: Timeout                     = Timeout(10.seconds)

  val logger: Logger = LoggerFactory.getLogger("quartery-filing-authorization-spec")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
  }

  override def afterAll(): Unit = super.afterAll()

  "QuarterlyFilingAuthorization" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)

    val probe = TestProbe[InstitutionEvent]("institution-event-probe")

    def setupInstitution(i: Institution): Assertion = {
      val entity = InstitutionPersistence.selectInstitution(sharding, i.LEI, i.activityYear)
      entity ! CreateInstitution(i, probe.ref)
      val message = probe.expectMessageType[InstitutionCreated]
      message.i.LEI mustBe i.LEI
    }

    "allow the inner route to be called if the institution is allowed to do a quarterly filing" in {
      val institution = Institution.empty.copy(activityYear = 2019, LEI = "EXAMPLE-LEI", quarterlyFiler = true)
      setupInstitution(institution)

      val route = get {
        QuarterlyFilingAuthorization
          .quarterlyFilingAllowed(logger, sharding)("EXAMPLE-LEI", 2019) {
            complete(OK)
          }
      }

      Get("/") ~> route ~> check {
        response.status mustBe OK
      }
    }

    "prevent the inner route from being called if the institution is not a quarterly filer" in {
      val institution =
        Institution.empty.copy(activityYear = 2019, LEI = "EXAMPLE-LEI-2", quarterlyFiler = false, quarterlyFilerHasFiledQ1 = true)
      setupInstitution(institution)

      val route = get {
        QuarterlyFilingAuthorization
          .quarterlyFilingAllowed(logger, sharding)(institution.LEI, institution.activityYear) {
            complete(OK)
          }
      }

      Get("/") ~> route ~> check {
        response.status mustBe Forbidden
      }
    }

    "prevent the inner route from being called if the institution has not yet been created" in {
      val route = get {
        QuarterlyFilingAuthorization
          .quarterlyFilingAllowed(logger, sharding)("LEI-OF-INSTITUTION-NOT-CREATED", 2019) {
            complete(OK)
          }
      }

      Get("/") ~> Route.seal(route) ~> check {
        response.status mustBe BadRequest
      }
    }
  }
}