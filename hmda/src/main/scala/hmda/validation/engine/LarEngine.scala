package hmda.validation.engine

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.validation.{
  Quality,
  Syntactical,
  ValidationErrorType,
  Validity
}
import hmda.validation.api.ValidationApi
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.syntactical.S300
import hmda.validation.rules.lar.validity._

object LarEngine extends ValidationApi[LoanApplicationRegister] {

  def validateLar(
      lar: LoanApplicationRegister): HmdaValidation[LoanApplicationRegister] = {
    val validations = Vector(
      checkSyntactical(lar),
      checkValidity(lar) //,
      //checkQuality(lar)
    )

    validations.par.reduceLeft(_ combine _)
  }

  def checkSyntactical(
      lar: LoanApplicationRegister): HmdaValidation[LoanApplicationRegister] = {
    val syntacticalChecks = Vector(
      S300
    )

    runChecks(lar, syntacticalChecks, Syntactical)
  }

  def checkValidity(
      lar: LoanApplicationRegister): HmdaValidation[LoanApplicationRegister] = {
    val validityChecks = Vector(
      V600,
      V610_1,
      V610_2,
      V611,
      V612_1,
      V612_2,
      V613_1,
      V613_2,
      V613_3,
      V613_4,
      V614_1,
      V615_1,
      V615_2,
      V615_3,
      V616,
      V617,
      V618,
      V620,
      V621,
      V623,
      V628_1,
      V628_2,
      V628_3,
      V628_4,
      V630,
      V633,
      V634,
      V635_1,
      V635_2,
      V635_3,
      V635_4,
      V637,
      V640,
      V642_1,
      V642_2,
      V643,
      V645,
      V646_1,
      V646_2,
      V647,
      V649,
      V651_1,
      V651_2,
      V652_1,
      V652_2,
      V659,
      V691,
      V695
    )

    runChecks(lar, validityChecks, Validity)

  }

  def checkQuality(
      lar: LoanApplicationRegister): HmdaValidation[LoanApplicationRegister] = {
    val qualityChecks = Vector(
      )

    runChecks(lar, qualityChecks, Quality)
  }

  private def runChecks(lar: LoanApplicationRegister,
                        checksToRun: Vector[EditCheck[LoanApplicationRegister]],
                        validationErrorType: ValidationErrorType)
    : HmdaValidation[LoanApplicationRegister] = {
    val checks = checksToRun.par
      .map(check(_, lar, lar.loan.ULI, validationErrorType))
      .toList
    checks.par.reduceLeft(_ combine _)
  }

}
