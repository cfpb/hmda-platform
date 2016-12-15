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
      val updated = MacroEditJustificationLookup.updateJustifications("Q007", Seq(justification))
      updated.size mustBe 3
      updated.contains(justification) mustBe true
    }
    "Look up list of justifications for a single edit and update it with a new list of justifications" in {
      val macroEditJustifications = Seq(
        MacroEditJustification(
          id = 1,
          value = "There were a large number of applications, but few loans were closed",
          verified = true
        ),
        MacroEditJustification(
          id = 3,
          value = "Loan activity for this filing year consisted mainly of purchased loans.",
          verified = true
        )
      )
      MacroEditJustificationLookup.updateJustifications("Q007", macroEditJustifications) mustBe
        Seq(
          MacroEditJustification(
            id = 1,
            value = "There were a large number of applications, but few loans were closed",
            verified = true
          ),
          MacroEditJustification(
            id = 2,
            value = "There were a large number of applications, but few loans were closed",
            verified = false
          ),
          MacroEditJustification(
            id = 3,
            value = "Loan activity for this filing year consisted mainly of purchased loans.",
            verified = true
          )
        )

    }
  }
}
