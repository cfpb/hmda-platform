package hmda.persistence.institutions

import akka.testkit.typed.scaladsl.TestProbe
import hmda.model.institution.Institution
import hmda.model.institutions.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institutions.InstitutionPersistence.{
  CreateInstitution,
  InstitutionCreated,
  InstitutionEvent
}

class InstitutionAsyncPersistenceSpec extends AkkaCassandraPersistenceSpec {

  val institutionProbe = TestProbe[InstitutionEvent]("institutions-probe")

  val sampleInstitution = institutionGen.sample.getOrElse(Institution.empty)

  "An institution" must {
    "Be created" in {
      val institutionPersistence =
        spawn(InstitutionPersistence.behavior("ABC12345"))
      institutionPersistence ! CreateInstitution(sampleInstitution,
                                                 institutionProbe.ref)
      institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))
    }
  }

}
