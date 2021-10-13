package hmda.uli.api.model

object ULIModel {

  case class Loan(loanId: String)
  case class ULI(loanId: String, checkDigit: String, uli: String) {
    def toCSV: String = s"$loanId,$checkDigit,$uli"
  }
  case class LoanCheckDigitResponse(loanIds: Seq[ULI])
  case class ULICheck(uli: String)
  case class ULIValidated(isValid: Boolean)
  case class ULIBatchValidated(uli: String, isValid: String) {
    def toCSV: String = s"$uli,$isValid"
  }
  case class ULIBatchValidatedResponse(ulis: Seq[ULIBatchValidated])
}
