package hmda.validation.model

sealed trait ResponseMessage {
  def isValid: Boolean
}

object AsyncModel {

  case class ULIValidate(uli: String)
  case class ULIValidated(isValid: Boolean) extends ResponseMessage
  case class TractValidate(tract: String)
  case class Tractvalidated(isValid: Boolean) extends ResponseMessage
  case class CountyValidate(county: String)
  case class Countyvalidated(isValid: Boolean) extends ResponseMessage
}
