package hmda.persistence.institution

import akka.actor
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import org.scalatest.Tag

object CustomTag extends Tag("actions-ignore")

class InstitutionPersistenceSpec extends AkkaCassandraPersistenceSpec {

  override val system                               = actor.ActorSystem()
  override implicit val typedSystem: ActorSystem[_] = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  InstitutionPersistence.startShardRegion(sharding)

  val institutionProbe = TestProbe[InstitutionEvent]("institutions-probe")
  val maybeInstitutionProbe =
    TestProbe[Option[Institution]]("institution-get-probe")
  val sampleInstitution =
    institutionGen
      .suchThat(i => i.LEI != "")
      .suchThat(i => i.activityYear == 2018)
      .sample
      .getOrElse(Institution.empty.copy(LEI = ""))
  val modified =
    sampleInstitution.copy(emailDomains = List("bank.com"))

  "An institution" must {
    "be created and read back" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)
      institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }

    "not be created if it already exists" taggedAs CustomTag in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)
      institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! CreateInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(modified))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }

    "be modified and read back" in {

      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)
      institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
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
      institutionProbe.expectMessage(InstitutionNotExists(modified.LEI))
    }

    "be deleted" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("ABC12345"), actorName)

      institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! DeleteInstitution(modified.LEI, modified.activityYear, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionDeleted(modified.LEI, modified.activityYear))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(None)
    }

    "not be deleted if it doesn't exist" in {
      val institutionPersistence =
        system.spawn(InstitutionPersistence.behavior("XXXXX"), actorName)

      institutionPersistence ! DeleteInstitution(modified.LEI, modified.activityYear, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionNotExists(modified.LEI))
    }

  }

  "A Sharded Institution" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be created and read back" in {
      val institutionPersistence =
        sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-ABC12345")

      institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(sampleInstitution))
    }
    "be modified and read back" in {
      val institutionPersistence =
        sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-ABC12345")

      institutionPersistence ! ModifyInstitution(modified, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionModified(modified))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(Some(modified))
    }
    "be deleted" in {
      val institutionPersistence =
        sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-ABC12345")

      institutionPersistence ! DeleteInstitution(modified.LEI, modified.activityYear, institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionDeleted(modified.LEI, modified.activityYear))

      institutionPersistence ! GetInstitution(maybeInstitutionProbe.ref)
      maybeInstitutionProbe.expectMessage(None)
    }
  }

}