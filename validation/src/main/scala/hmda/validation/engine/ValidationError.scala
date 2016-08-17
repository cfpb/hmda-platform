package hmda.validation.engine

case class ValidationError(id: String, msg: String)
case class ValidationErrors(errors: Seq[ValidationError])
