package hmda.api.http.filing

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.model.institution.{Institution, InstitutionDetail}
import hmda.persistence.AkkaCassandraPersistenceSpec
import org.scalatest.MustMatchers
import akka.actor.typed.scaladsl.adapter._
import akka.testkit._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.codec.filing.FilingStatusCodec._
import io.circe.generic.auto._
import hmda.model.institution.InstitutionGenerators._
import hmda.model.filing.FilingGenerator._
import hmda.model.institution.Institution

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import hmda.messages.filing.FilingCommands.CreateFiling
import hmda.messages.filing.FilingEvents.{FilingCreated, FilingEvent}
import hmda.persistence.institution.InstitutionPersistence
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionEvent
}
import hmda.model.filing.{Filing, InProgress}
import hmda.persistence.filing.FilingPersistence

class InstitutionHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with InstitutionHttpApi
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
  val filingProbe = TestProbe[FilingEvent]("filing-probe")

  val sampleFiling = filingGen.sample
    .getOrElse(Filing())
    .copy(lei = sampleInstitution.LEI)
    .copy(period = period)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    val institutionPersistence = sharding.entityRefFor(
      InstitutionPersistence.typeKey,
      s"${InstitutionPersistence.name}-${sampleInstitution.LEI}")
    institutionPersistence ! CreateInstitution(sampleInstitution,
                                               institutionProbe.ref)
    institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))
    val filingPersistence =
      sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${sampleInstitution.LEI}-$period"
      )
    filingPersistence ! CreateFiling(sampleFiling, filingProbe.ref)

    filingProbe.expectMessage(FilingCreated(sampleFiling))
  }

  override def afterAll(): Unit = super.afterAll()

  val url = s"/institutions/${sampleInstitution.LEI}"
  val badUrl = s"/institutions/xxxx"

  "Institutions" must {
    "return NotFound when institution does not exist" in {
      Get(badUrl) ~> institutionRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "return Institution when found" in {
      Get(url) ~> institutionRoutes ~> check {
        val details = responseAs[InstitutionDetail]
        details.filings.head.status mustBe InProgress
      }
    }
  }

}
