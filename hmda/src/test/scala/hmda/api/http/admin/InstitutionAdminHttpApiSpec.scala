package hmda.api.http.admin

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.{LoggingAdapter, NoLogging}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.model.institution.Institution
import org.scalatest.MustMatchers
import hmda.model.institution.InstitutionGenerators._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class InstitutionAdminHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with InstitutionAdminHttpApi
    with ScalatestRouteTest {

  val duration = 10.seconds

  override implicit val typedSystem: ActorSystem[_] = system.toTyped
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(duration)
  override val sharding: ClusterSharding = ClusterSharding(typedSystem)

  override def beforeAll(): Unit = super.beforeAll()

  override def afterAll(): Unit = super.afterAll()

  val lei = "AAA"
  val sampleInstitution =
    institutionGen.sample.getOrElse(Institution.empty).copy(LEI = Some(lei))

  "Institutions HTTP Service" must {
    "return OPTIONS" in {
      Options("/institutions") ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }

}
