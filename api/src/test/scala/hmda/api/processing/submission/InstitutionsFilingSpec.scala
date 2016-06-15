package hmda.api.processing.submission

import akka.testkit.TestProbe
import hmda.api.demo.DemoData
import hmda.api.processing.{ ActorSpec, CommonMessages }
import CommonMessages.GetState
import hmda.api.model.processing.Institutions
import hmda.api.processing.submission.InstitutionsFiling._

class InstitutionsFilingSpec extends ActorSpec {

  val institutionActor = createInstitutionsFiling(system)

  val probe = TestProbe()

  "Institution Filings" must {
    "be created and read back" in {
      val institutions = DemoData.institutions
      for (institution <- institutions) {
        probe.send(institutionActor, CreateInstitution(institution))
      }
      probe.send(institutionActor, GetState)
      probe.expectMsg(Institutions(institutions))
    }
  }
}
