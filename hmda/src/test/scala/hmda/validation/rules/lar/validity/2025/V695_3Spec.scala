package hmda.validation.rules.lar.validity._2025

import hmda.model.filing.lar.LarGenerators.{larGen, larNGen}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity._2025.V695_3

class V695_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V695_3

  property("NMLSR Identifier must between four and twelve digits.") {
    val larList   = larNGen(1).suchThat(_.nonEmpty).sample.getOrElse(Nil)
    val lar = larList(0)

     val larPass =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "1234567"))
    larPass.mustPass

    val larPassAlt =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "125739285735"))
    larPassAlt.mustPass

    val larPassAnotherAlt =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "1234"))
    larPassAnotherAlt.mustPass


    val larFail =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "125e739285735"))
    larFail.mustFail

    val larFailAlt =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "12"))
    larFailAlt.mustFail

    val larFailAnotherAlt =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "45.56"))
    larFailAnotherAlt.mustFail

    val larFailFinal =lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "Whazam"))
    larFailFinal.mustFail


  }
}