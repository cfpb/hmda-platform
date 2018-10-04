package hmda.api.http.filing

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
import akka.testkit._
import hmda.persistence.filing.FilingPersistence
import hmda.api.http.codec.filing.FilingCodec._
import hmda.persistence.institution.InstitutionPersistence
import io.circe.generic.auto._

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

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
  }

  override def afterAll(): Unit = super.afterAll()

  "Filings" must {
    "return error when institution does not exist" in {
      Get("/institutions/xxxx/filings/2018") ~> filingRoutes ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
  }
}
