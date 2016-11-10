package hmda.api.protocol.fi.lar

import hmda.model.fi.lar._
import hmda.parser.fi.lar.{ LarParsingError, ParsingErrorSummary }
import spray.json.DefaultJsonProtocol

trait LarProtocol extends DefaultJsonProtocol {
  implicit val loanFormat = jsonFormat7(Loan.apply)
  implicit val geographyFormat = jsonFormat4(Geography.apply)
  implicit val applicantFormat = jsonFormat15(Applicant.apply)
  implicit val denialFormat = jsonFormat3(Denial.apply)
  implicit val larFormat = jsonFormat14(LoanApplicationRegister.apply)
  implicit val larParsingErrorFormat = jsonFormat2(LarParsingError.apply)
  implicit val parsingErrorSummaryFormat = jsonFormat2(ParsingErrorSummary.apply)
}
