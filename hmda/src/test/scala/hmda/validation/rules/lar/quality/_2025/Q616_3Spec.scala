package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.Conventional
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.quality._2025.Q616_3

import scala.util.Try

class Q616_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q616_3

  property("The Discount Points should not generally be higher than 15% of the Loan Amount and must be reported in dollars.") {

    val larList   = larNGen(1).suchThat(_.nonEmpty).sample.getOrElse(Nil)
    val lar = larList(0)

     val larPass =lar.copy(loan = lar.loan.copy(amount = 645245.00),
       loanDisclosure = lar.loanDisclosure.copy(discountPoints = "96786.75"))
      larPass.mustPass

      val larFail =lar.copy(loan = lar.loan.copy(amount = 645245.00),
        loanDisclosure = lar.loanDisclosure.copy(discountPoints = "99786.75"))
      larFail.mustFail

      val larPassAlt =lar.copy(loan = lar.loan.copy(amount = 645245.00),
        loanDisclosure = lar.loanDisclosure.copy(discountPoints = "4586.75"))
    larPassAlt.mustPass

      val larFailAlt =lar.copy(loan = lar.loan.copy(amount = 645245.00),
        loanDisclosure = lar.loanDisclosure.copy(discountPoints = "444586.75"))
      larFailAlt.mustFail


  }
}