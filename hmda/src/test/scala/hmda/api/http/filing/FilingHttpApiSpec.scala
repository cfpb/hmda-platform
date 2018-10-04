package hmda.api.http.filing

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import hmda.persistence.AkkaCassandraPersistenceSpec
import org.scalatest.MustMatchers
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import hmda.persistence.filing.FilingPersistence
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.codec.filing.FilingCodec._
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionEvent
}
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence
import io.circe.generic.auto._
import hmda.model.institution.InstitutionGenerators._
import akka.testkit._
import hmda.model.filing.{FilingDetails, InProgress}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class FilingHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with FilingHttpApi
    with ScalatestRouteTest {

  val duration = 10.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  override implicit val typedSystem: ActorSystem[_] = system.toTyped
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(duration)
  override val sharding: ClusterSharding = ClusterSharding(typedSystem)

  val sampleInstitution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty.copy(LEI = "AAA"))

  val period = "2018"

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    val institutionPersistence =
      sharding.entityRefFor(
        InstitutionPersistence.typeKey,
        s"${InstitutionPersistence.name}-${sampleInstitution.LEI}")
    institutionPersistence ! CreateInstitution(sampleInstitution,
                                               institutionProbe.ref)
    institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))
  }

  override def afterAll(): Unit = super.afterAll()
  val url = s"/institutions/${sampleInstitution.LEI}/filings/$period"
  val badUrl = s"/institutions/xxxx/filings/$period"

  "Filings" must {
    "return Bad Request when institution does not exist" in {
      Get(badUrl) ~> filingRoutes ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
    "return NotFound when institution exists but filing has not been created" in {
      Get(url) ~> filingRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "create filing and return it" in {
      Post(url) ~> filingRoutes ~> check {
        status mustBe StatusCodes.OK
        val details = responseAs[FilingDetails]
        details.filing.lei mustBe sampleInstitution.LEI
        details.filing.period mustBe period
        details.filing.status mustBe InProgress
        details.submissions mustBe Nil
      }
      Post(url) ~> filingRoutes ~> check {
        status mustBe StatusCodes.BadRequest
      }
      Post(badUrl) ~> filingRoutes ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
  }
}
