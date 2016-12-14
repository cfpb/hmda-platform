package hmda.api.model

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
  }
}
