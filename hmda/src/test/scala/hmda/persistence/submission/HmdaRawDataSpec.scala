package hmda.persistence.submission

import java.time.Instant

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import hmda.messages.submission.HmdaRawDataCommands.AddLine
import hmda.messages.submission.HmdaRawDataEvents.{HmdaRawDataEvent, LineAdded}
import hmda.model.filing.submission.SubmissionId
import hmda.utils.YearUtils.Period

class HmdaRawDataSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  HmdaRawData.startShardRegion(sharding)

  val hmdaRawProbe = TestProbe[HmdaRawDataEvent]

  val submissionId = SubmissionId("12345", Period(2018, None), 1)

  "HMDA Raw Data" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be persisted" in {
      val hmdaRawData =
        sharding.entityRefFor(HmdaRawData.typeKey,
                              s"${HmdaRawData.name}-$submissionId")

      val timestamp = Instant.now.toEpochMilli

      hmdaRawData ! AddLine(submissionId,
                            timestamp,
                            "data",
                            Some(hmdaRawProbe.ref))

      hmdaRawProbe.expectMessage(LineAdded(timestamp, "data"))
    }
  }

}
