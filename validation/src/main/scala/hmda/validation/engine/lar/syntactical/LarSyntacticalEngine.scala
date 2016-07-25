package hmda.validation.engine.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.ts.syntactical.S025
import hmda.validation.rules.lar.syntactical._

trait LarSyntacticalEngine extends LarCommonEngine with ValidationApi {

  private def s025(lar: LoanApplicationRegister, institution: Institution): LarValidation = {
    convertResult(lar, S025(lar, institution), "S025")
  }

  def checkSyntactical(lar: LoanApplicationRegister, institution: Option[Institution]): LarValidation = {
    val checks = List(
      S010,
      S020,
      S205
    ).map(check(_, lar))

    // Exclude edits requiring institution data
    if (institution.isDefined) checks :+ s025(lar, institution.get)

    validateAll(checks, lar)
  }

  def checkSyntacticalCollection(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    val checks = List(
      S011,
      S040
    ).map(check(_, lars))

    validateAll(checks, lars)
  }

}
