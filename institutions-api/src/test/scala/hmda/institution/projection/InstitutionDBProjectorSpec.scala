package hmda.institution.projection

import akka.actor.ActorSystem
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.messages.projection.CommonProjectionMessages.{
  GetOffset,
  OffsetSaved,
  ProjectionEvent,
  SaveOffset
}

class InstitutionDBProjectorSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = ActorSystem()
  override implicit val typedSystem = system.toTyped

  val probe = TestProbe[ProjectionEvent](name = "institution-projector-probe")

  "Institution Projector" must {
    "retrieve 0 for empty projection" in {
      val institutionDBProjector =
        system.spawn(InstitutionDBProjector.behavior, actorName)
      institutionDBProjector ! GetOffset(probe.ref)
      probe.expectMessage(OffsetSaved(0L))
    }

    "update offset for projection" in {
      val institutionDBProjector =
        system.spawn(InstitutionDBProjector.behavior, actorName)
      institutionDBProjector ! SaveOffset(1L, probe.ref)
      institutionDBProjector ! GetOffset(probe.ref)
      probe.expectMessage(OffsetSaved(1L))
    }
  }

}
