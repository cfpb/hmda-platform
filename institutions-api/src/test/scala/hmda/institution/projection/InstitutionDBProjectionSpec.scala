package hmda.institution.projection

import akka.actor.ActorSystem
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.persistence.query.{NoOffset, TimeBasedUUID}
import com.datastax.driver.core.utils.UUIDs
import hmda.messages.projection.CommonProjectionMessages.{
  GetOffset,
  OffsetSaved,
  ProjectionEvent,
  SaveOffset
}

class InstitutionDBProjectionSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = ActorSystem()
  override implicit val typedSystem = system.toTyped

  val probe = TestProbe[ProjectionEvent](name = "institution-projector-probe")

  "Institution Projector" must {
    "retrieve offset for empty projection" in {
      val institutionDBProjector =
        system.spawn(InstitutionDBProjection.behavior, actorName)
      institutionDBProjector ! GetOffset(probe.ref)
      probe.expectMessage(OffsetSaved(NoOffset))
    }

    "update offset for projection" in {
      val uuid = UUIDs.timeBased()
      val offset = TimeBasedUUID(uuid)
      val institutionDBProjector =
        system.spawn(InstitutionDBProjection.behavior, actorName)
      institutionDBProjector ! SaveOffset(offset, probe.ref)
      institutionDBProjector ! GetOffset(probe.ref)
      probe.expectMessage(OffsetSaved(offset))
    }
  }

}
