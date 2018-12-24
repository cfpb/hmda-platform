package hmda.validation.model

sealed trait RequestMessage

sealed trait ResponseMessage {
  def isValid: Boolean
}

object AsyncModel {

  case class ULIValidate(uli: String) extends RequestMessage
  case class ULIValidated(isValid: Boolean) extends ResponseMessage
  case class TractValidate(tract: String) extends RequestMessage
  case class Tractvalidated(isValid: Boolean) extends ResponseMessage
  case class CountyValidate(county: String) extends RequestMessage
  case class Countyvalidated(isValid: Boolean) extends ResponseMessage
}
