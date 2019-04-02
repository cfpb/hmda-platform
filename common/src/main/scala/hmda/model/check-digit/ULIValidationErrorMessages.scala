package hmda.uli.api.model

object ULIValidationErrorMessages {
  final val invalidLoanIdLengthMessage =
    "Loan ID is not between 21 and 43 characters long"
  final val nonAlpanumericLoanIdMessage = "Loan ID is not alphanumeric"
  final val invalidULILengthMessage =
    "ULI is not between 23 and 45 characters long"
  final val nonAlphanumericULIMessage = "ULI is not alphanumeric"
}
