package hmda.api.protocol.public

import hmda.api.model.public.ULIModel.{ Loan, ULI, ULICheck, ULIValidated }
import spray.json.DefaultJsonProtocol

trait ULIProtocol extends DefaultJsonProtocol {

  implicit val loanFormat = jsonFormat1(Loan.apply)
  implicit val uliFormat = jsonFormat3(ULI.apply)
  implicit val uliCheckFormat = jsonFormat1(ULICheck.apply)
  implicit val uliValidated = jsonFormat1(ULIValidated.apply)

}
