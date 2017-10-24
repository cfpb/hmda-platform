package hmda.api.protocol.public

import hmda.api.model.public.ULIModel._
import spray.json.DefaultJsonProtocol

trait ULIProtocol extends DefaultJsonProtocol {

  implicit val loanFormat = jsonFormat1(Loan.apply)
  implicit val uliFormat = jsonFormat3(ULI.apply)
  implicit val uliCheckFormat = jsonFormat1(ULICheck.apply)
  implicit val uliValidatedFormat = jsonFormat1(ULIValidated.apply)
  implicit val uliBatchValidatedFormat = jsonFormat2(ULIBatchValidated.apply)
  implicit val uliBatchValidatedResponseFormat = jsonFormat1(ULIBatchValidatedResponse.apply)

}
