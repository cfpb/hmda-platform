package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import hmda.model.edits.{EditDetail, EditDetailRow}
import hmda.model.filing.submission.SubmissionId

class EditDetailsPersistenceSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)

  val editDetailProbe = TestProbe[EditDetailPersistenceEvent]("edit-detail")
  val editRowCountProbe = TestProbe[Int]("row-count")

  override def beforeAll(): Unit = {
    super.beforeAll()
    EditDetailPersistence.startShardRegion(sharding)
  }

  "Edit Details" must {
    val submissionId = SubmissionId("ABCD", "2018", 1)
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be persisted and read back" in {
      val editDetail1 = EditDetail("S300",
                                   Seq(
                                     EditDetailRow("1"),
                                     EditDetailRow("2")
                                   ))
      val editDetail2 = EditDetail("S301",
                                   Seq(
                                     EditDetailRow("1")
                                   ))

      val editDetailPersistence =
        sharding.entityRefFor(EditDetailPersistence.typeKey,
                              s"${EditDetailPersistence.name}-$submissionId")

      editDetailPersistence ! PersistEditDetail(editDetail1,
                                                Some(editDetailProbe.ref))
      editDetailProbe.expectMessage(EditDetailAdded(editDetail1))

      editDetailPersistence ! PersistEditDetail(editDetail2,
                                                Some(editDetailProbe.ref))
      editDetailProbe.expectMessage(EditDetailAdded(editDetail2))

      editDetailPersistence ! GetEditRowCount("S300", editRowCountProbe.ref)
      editRowCountProbe.expectMessage(2)

      editDetailPersistence ! GetEditRowCount("S301", editRowCountProbe.ref)
      editRowCountProbe.expectMessage(1)

    }
  }

}
