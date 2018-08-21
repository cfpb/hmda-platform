package hmda.api.http.admin

import akka.actor.typed.{ActorSystem, Props}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.event.{LoggingAdapter, NoLogging}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.ClusterShardingSettings
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import hmda.model.institution.Institution
import org.scalatest.MustMatchers
import hmda.model.institution.InstitutionGenerators._
import hmda.api.http.codec.institution.InstitutionCodec._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.persistence.institution.InstitutionPersistence
import akka.testkit._
import com.typesafe.config.ConfigFactory
import hmda.api.http.model.admin.InstitutionDeletedResponse
import hmda.messages.institution.InstitutionCommands.{
  InstitutionCommand,
  InstitutionStop
}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class InstitutionAdminHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with InstitutionAdminHttpApi
    with ScalatestRouteTest {

  val duration = 10.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  override implicit val typedSystem: ActorSystem[_] = system.toTyped
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(duration)
  override val sharding: ClusterSharding = ClusterSharding(typedSystem)

  override def beforeAll(): Unit = {
    val config = ConfigFactory.load()
    val shardNumber = config.getInt("hmda.institutions.shardNumber")
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    sharding.spawn(
      behavior = entityId => InstitutionPersistence.behavior(entityId),
      Props.empty,
      EntityTypeKey[InstitutionCommand](InstitutionPersistence.name),
      ClusterShardingSettings(typedSystem),
      maxNumberOfShards = shardNumber,
      handOffStopMessage = InstitutionStop
    )
  }

  override def afterAll(): Unit = super.afterAll()

  val lei = "AAA"
  val sampleInstitution =
    institutionGen.sample.getOrElse(Institution.empty).copy(LEI = Some(lei))

  val modified =
    sampleInstitution.copy(emailDomains = List("email@bank.com"))

  "Institutions HTTP Service" must {

    "Respond to Options request" in {
      Options("/institutions") ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

    "Create an institution" in {
      Post("/institutions", sampleInstitution) ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Get an institution" in {
      Get(s"/institutions/${sampleInstitution.LEI.getOrElse("")}") ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Modify an institution" in {

      Put("/institutions", modified) ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[Institution] mustBe modified
      }
    }

    "Delete an institution" in {
      Delete("/institutions", modified) ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[InstitutionDeletedResponse] mustBe InstitutionDeletedResponse(
          lei)
      }
    }

  }

}
