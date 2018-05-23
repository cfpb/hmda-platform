package hmda.persistence.institution

import akka.actor.typed.{ActorSystem, Props}
import akka.cluster.sharding.typed.ClusterShardingSettings
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.testkit.typed.scaladsl.TestProbe
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institution.InstitutionPersistence._

class InstitutionAsyncPersistenceSpec extends AkkaCassandraPersistenceSpec {

  val institutionProbe = TestProbe[InstitutionEvent]("institutions-probe")
  val maybeInstitutionProbe =
    TestProbe[Option[Institution]]("institution-get-probe")
  val sampleInstitution = institutionGen.sample.getOrElse(Institution.empty)
  val modified =
    sampleInstitution.copy(emailDomain = Some("sample@bank.com"))

  val sharding = ClusterSharding(system)

  "An institution" must {
    "be created and read back" in {
      val institutionPersistence =
        spawn(InstitutionPersistence.behavior("ABC12345"))
      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! Get(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }

    "be modified and read back" in {
      val institutionPersistence =
        spawn(InstitutionPersistence.behavior("ABC12345"))

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionModified(modified))

      institutionPersistence ! Get(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(modified))
    }

    "not be modified if it doesn't exist" in {
      val institutionPersistence =
        spawn(InstitutionPersistence.behavior("XXXXX"))

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionNotExists)
    }

    "be deleted" in {
      val institutionPersistence =
        spawn(InstitutionPersistence.behavior("ABC12345"))

      institutionPersistence ! DeleteInstitution(modified.LEI.getOrElse(""),
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(
        InstitutionDeleted(modified.LEI.getOrElse("")))

      institutionPersistence ! Get(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(None)
    }

    "not be deleted if it doesn't exist" in {
      val institutionPersistence =
        spawn(InstitutionPersistence.behavior("XXXXX"))

      institutionPersistence ! DeleteInstitution(modified.LEI.getOrElse(""),
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionNotExists)
    }

  }

  "A Sharded Institution" must {
    Cluster(system).manager ! Join(Cluster(system).selfMember.address)
    "be created and read back" in {
      val institutionPersistence =
        createShardedInstitution(system, "ABC12345")

      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! Get(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }
    "be modified and read back" in {
      val institutionPersistence =
        createShardedInstitution(system, "ABC12345")

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionModified(modified))

      institutionPersistence ! Get(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(modified))
    }
    "be deleted" in {
      val institutionPersistence =
        createShardedInstitution(system, "ABC12345")

      institutionPersistence ! DeleteInstitution(modified.LEI.getOrElse(""),
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(
        InstitutionDeleted(modified.LEI.getOrElse("")))

      institutionPersistence ! Get(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(None)
    }
  }

  private def createShardedInstitution(
      system: ActorSystem[_],
      entityId: String): EntityRef[InstitutionCommand] = {
    ClusterSharding(system).spawn[InstitutionCommand](
      entityId => behavior(entityId),
      Props.empty,
      ShardingTypeName,
      ClusterShardingSettings(system),
      maxNumberOfShards = shardNumber,
      handOffStopMessage = InstitutionStop
    )

    ClusterSharding(system).entityRefFor(ShardingTypeName, entityId)
  }

}
