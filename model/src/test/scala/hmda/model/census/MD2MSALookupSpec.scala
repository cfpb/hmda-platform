package hmda.model.census

import org.scalatest.{ MustMatchers, WordSpec }

class MD2MSALookupSpec extends WordSpec with MustMatchers {

  "MSA lookup" must {
    "Provide lookup between MSA <-> MD" in {
      val lookup = MD2MSALookup.values
      lookup.filter(_.md == "31084").head.msa mustBe "31080"
    }
  }
}
