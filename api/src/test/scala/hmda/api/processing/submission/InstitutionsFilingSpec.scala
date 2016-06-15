package hmda.api.processing.submission

import akka.testkit.TestProbe
import hmda.api.util.TestData
import hmda.api.processing.{ ActorSpec, CommonMessages }
import CommonMessages.GetState
import hmda.api.model.processing.Institutions
import hmda.api.processing.submission.InstitutionsFiling._

class InstitutionsFilingSpec extends ActorSpec {

  val institutionActor = createInstitutionsFiling(system)

  val probe = TestProbe()

  "Institution Filings" must {
    "be created and read back" in {
      val institutions = TestData.institutions
      for (institution <- institutions) {
        probe.send(institutionActor, CreateInstitution(institution))
      }
      probe.send(institutionActor, GetState)
      probe.expectMsg(Institutions(institutions))
    }
    "be created, modified and read back" in {
      val institution = TestData.institutions.head
      probe.send(institutionActor, CreateInstitution(institution))
      val modified = institution.copy(name = "new name")
      probe.send(institutionActor, ModifyInstitution(modified))
      probe.send(institutionActor, GetInstitutionByIdAndPeriod(modified.id, modified.period))
      probe.expectMsg(modified)
    }
  }
}
