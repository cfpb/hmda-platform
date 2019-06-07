package hmda.data.browser.models

sealed trait ErrorResponse {
  def errorType: String
}
final case class InvalidRaces(invalidRaces: Seq[String],
                              errorType: String = "invalid-races")
    extends ErrorResponse
final case class InvalidActions(invalidActions: Seq[String],
                                errorType: String = "invalid-action-taken-type")
    extends ErrorResponse

final case class InvalidLoanTypes(invalidLoanTypes: Seq[String],
                                  errorType: String = "invalid-loan-types")
    extends ErrorResponse

final case class InvalidLoanPurposes(
    invalidLoanPurposes: Seq[String],
    errorType: String = "invalid-loan-purposes")
    extends ErrorResponse

final case class InvalidLienStatuses(
    invalidLienStatuses: Seq[String],
    errorType: String = "invalid-lien-statuses")
    extends ErrorResponse

final case class InvalidLoanProducts(
    invalidateLoanProducts: Seq[String],
    errorType: String = "invalid-loan-products")
    extends ErrorResponse

final case class InvalidDwellingCategories(
    invalidDwellingCategories: Seq[String],
    errorType: String = "invalid-dwelling-categories")
    extends ErrorResponse

final case class InvalidConstructionMethods(
    invalidConstructionMethods: Seq[String],
    errorType: String = "invalid-construction-methods")
    extends ErrorResponse

final case class InvalidSexes(invalidLoanTypes: Seq[String],
                              errorType: String = "invalid-sexes")
    extends ErrorResponse

final case class InvalidTotalUnits(invalidTotalUnits: Seq[String],
                                   errorType: String = "invalid-total-units")
    extends ErrorResponse

final case class InvalidEthnicities(invalidTotalUnits: Seq[String],
                                    errorType: String = "invalid-ethnicities")
    extends ErrorResponse

final case class InvalidMsaMds(invalidMsaMds: Seq[String],
                               errorType: String = "invalid-msamds")
    extends ErrorResponse

final case class InvalidStates(invalidStates: Seq[String],
                               errorType: String = "invalid-states")
    extends ErrorResponse
