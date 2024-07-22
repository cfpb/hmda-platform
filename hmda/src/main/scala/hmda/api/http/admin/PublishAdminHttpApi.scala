package hmda.api.http.admin
// $COVERAGE-OFF$
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.auth.OAuth2Authorization
import hmda.publication.KafkaUtils._
import akka.Done
import akka.actor.typed.ActorSystem
import org.apache.kafka.clients.producer.{ Producer => KafkaProducer }
import hmda.messages.pubsub.HmdaTopics._
import hmda.util.http.FilingResponseUtils._

import scala.concurrent.{ ExecutionContext, Future }

object PublishAdminHttpApi {
  def create(sharding: ClusterSharding, config: Config)(implicit ec: ExecutionContext, sys: ActorSystem[_]): OAuth2Authorization => Route =
    new PublishAdminHttpApi(sharding, config)(ec, sys).publishAdminRoutes _
}

private class PublishAdminHttpApi(sharding: ClusterSharding, config: Config)(implicit ec: ExecutionContext, sys: ActorSystem[_]) {
  val hmdaAdminRole   = config.getString("keycloak.hmda.admin.role")

  val stringKafkaProducer = getStringKafkaProducer(sys)

  def publishAdminRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          publishTopicPath(oAuth2Authorization)
        }
      }
    }
  
  private def publishTopicPath(oAuth2Authorization: OAuth2Authorization): Route = {
    path("publish" / Segment / "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber) { (topic, lei, period, sequenceNumber) =>
        (extractUri & post)(uri =>
            oAuth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
                val submissionId = s"$lei-$period-$sequenceNumber"
                if (verifyTopic(topic)) {
                    val publish = publishKafkaEvent(topic, submissionId, lei, stringKafkaProducer)
                    complete((StatusCodes.Created, s"Topic ${topic} with data, ${lei}-${period}-${submissionId}, published"))
                } else {
                    invalidTopic(StatusCodes.BadRequest, topic, uri)
                }

            }
        )
        }
    }

    private def verifyTopic(topic: String): Boolean = {
        val validTopics = List(signTopic, modifiedLarTopic, irsTopic, analyticsTopic)
        validTopics.contains(topic)
    }

    private def publishKafkaEvent(
        topic: String,
        submissionId: String,
        lei: String,
        stringKafkaProducer: KafkaProducer[String, String]
        ): Future[Done] =
        for {
        _ <- produceRecord(topic, lei, submissionId, stringKafkaProducer)
        } yield Done
}
// $COVERAGE-ON$