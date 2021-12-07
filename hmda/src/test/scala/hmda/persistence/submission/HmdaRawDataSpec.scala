package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import hmda.messages.submission.HmdaRawDataCommands.AddLines
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import hmda.model.filing.submission.SubmissionId
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.utils.YearUtils.Period

import java.time.Instant

class HmdaRawDataSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system      = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  HmdaRawData.startShardRegion(sharding)

  val hmdaRawProbe = TestProbe[LinesAdded]()

  val submissionId = SubmissionId("12345", Period(2018, None), 1)

  "HMDA Raw Data" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be persisted" in {
      val hmdaRawData =
        sharding.entityRefFor(HmdaRawData.typeKey, s"${HmdaRawData.name}-$submissionId")

      val timestamp = Instant.now.toEpochMilli

      hmdaRawData ! AddLines(submissionId, timestamp, List("data1", "data2"), Some(hmdaRawProbe.ref))

      hmdaRawProbe.expectMessage(LinesAdded(List(LineAdded(timestamp, "data1"), LineAdded(timestamp, "data2"))))
    }
  }

}