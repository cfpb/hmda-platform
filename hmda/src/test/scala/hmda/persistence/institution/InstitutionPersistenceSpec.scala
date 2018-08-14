package hmda.persistence.institution

import akka.actor
import akka.actor.typed.{ActorSystem, Props}
import akka.cluster.sharding.typed.ClusterShardingSettings
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institution.InstitutionPersistence._
import akka.actor.typed.scaladsl.adapter._
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._

class InstitutionPersistenceSpec extends AkkaCassandraPersistenceSpec {

  override val system = actor.ActorSystem()
  override implicit val typedSystem: ActorSystem[_] = system.toTyped

  val sharding = ClusterSharding(typedSystem)

  val institutionProbe = TestProbe[InstitutionEvent]("institutions-probe")
  val maybeInstitutionProbe =
    TestProbe[Option[Institution]]("institution-get-probe")
  val sampleInstitution =
    institutionGen
      .suchThat(i => i.LEI.isDefined)
      .sample
      .getOrElse(Institution.empty.copy(LEI = Some("")))
  val modified =
    sampleInstitution.copy(emailDomains = List("sample@bank.com"))

  "An institution" must {
    "be created and read back" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)
      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }

    "be modified and read back" in {

      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)
      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionModified(modified))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(modified))
    }

    "not be modified if it doesn't exist" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("XXXXX"), actorName)

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(
        InstitutionNotExists(modified.LEI.getOrElse("")))
    }

    "be deleted" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)

      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! DeleteInstitution(modified.LEI.getOrElse(""),
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(
        InstitutionDeleted(modified.LEI.getOrElse("")))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(None)
    }

    "not be deleted if it doesn't exist" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("XXXXX"), actorName)

      institutionPersistence ! DeleteInstitution(modified.LEI.getOrElse(""),
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(
        InstitutionNotExists(modified.LEI.getOrElse("")))
    }

  }

  "A Sharded Institution" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be created and read back" in {
      val institutionPersistence =
        createShardedInstitution(typedSystem, "ABC12345")

      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }
    "be modified and read back" in {
      val institutionPersistence =
        createShardedInstitution(typedSystem, "ABC12345")

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionModified(modified))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(modified))
    }
    "be deleted" in {
      val institutionPersistence =
        createShardedInstitution(typedSystem, "ABC12345")

      institutionPersistence ! DeleteInstitution(modified.LEI.getOrElse(""),
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(
        InstitutionDeleted(modified.LEI.getOrElse("")))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
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
