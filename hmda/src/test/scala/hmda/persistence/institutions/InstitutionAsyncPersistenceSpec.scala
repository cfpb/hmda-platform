package hmda.persistence.institutions

import akka.testkit.typed.scaladsl.TestProbe
import hmda.model.institution.Institution
import hmda.model.institutions.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institutions.InstitutionPersistence._

class InstitutionAsyncPersistenceSpec extends AkkaCassandraPersistenceSpec {

  val institutionProbe = TestProbe[InstitutionEvent]("institutions-probe")
  val maybeInstitutionProbe =
    TestProbe[Option[Institution]]("institution-get-probe")
  val sampleInstitution = institutionGen.sample.getOrElse(Institution.empty)
  val modified =
    sampleInstitution.copy(emailDomain = Some("sample@bank.com"))

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

  }

}
