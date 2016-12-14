package hmda.api.model

import hmda.validation.engine.MacroEditJustification
import org.scalatest.{ MustMatchers, WordSpec }

class MacroEditJustificationLookupSpec extends WordSpec with MustMatchers {
  "Macro Edit Justifications" must {
    "Look up list of justifications for a single edit" in {
      val lookup = MacroEditJustificationLookup()
      val q23Just = lookup.justifications.filter(j => j.edit == "Q023")
      val q31Just = lookup.justifications.filter(j => j.edit == "Q031")
      q23Just.size mustBe 4
      q31Just.size mustBe 2
    }
    "Look up list of justifications for a single edit and update it with a new justification" in {
      val justification = MacroEditJustification(
        id = 2,
        value = "There were a large number of applications, but few loans were closed",
        verified = true
      )
      val lookup = MacroEditJustificationLookup()
      val justificationWithName = MacroEditJustificationWithName("Q007", justification)
      val updated = lookup.update(justificationWithName)
      updated.justifications.size mustBe 3
      updated.justifications.contains(MacroEditJustificationWithName("Q007", justification)) mustBe true
    }
  }
}
