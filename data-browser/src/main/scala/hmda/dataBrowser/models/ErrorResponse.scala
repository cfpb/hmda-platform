package hmda.dataBrowser.models

import hmda.census.records.CensusRecords
import io.circe.Encoder
import io.circe.generic.semiauto._

// $COVERAGE-OFF$
sealed trait ErrorResponse {
  def errorType: String
  def message: String
}

object InvalidRaces {
  implicit val encoder: Encoder[InvalidRaces] = deriveEncoder[InvalidRaces]
}
final case class InvalidRaces(
                               invalidRaces: Seq[String],
                               errorType: String = "invalid-races",
                               message: String = s"valid races are ${Race.values.map(_.entryName).mkString(", ")}"
                             ) extends ErrorResponse

object InvalidActions {
  implicit val encoder: Encoder[InvalidActions] = deriveEncoder[InvalidActions]
}
final case class InvalidActions(
                                 invalidActions: Seq[String],
                                 errorType: String = "invalid-action-taken-type",
                                 message: String = s"valid actions are ${ActionTaken.values.map(_.entryName).mkString(", ")}"
                               ) extends ErrorResponse

object InvalidMsaMds {
  implicit val encoder: Encoder[InvalidMsaMds] = deriveEncoder[InvalidMsaMds]
}
final case class InvalidMsaMds(invalidMsaMds: Seq[String], errorType: String = "invalid-msamds", message: String = "") extends ErrorResponse

object InvalidLEIs {
  implicit val encoder: Encoder[InvalidLEIs] = deriveEncoder[InvalidLEIs]
}
final case class InvalidLEIs(invalidLEIs: Seq[String], errorType: String = "invalid-LEIs", message: String = "") extends ErrorResponse

object InvalidStates {
  implicit val encoder: Encoder[InvalidStates] = deriveEncoder[InvalidStates]
}
final case class InvalidStates(
                                invalidStates: Seq[String],
                                errorType: String = "invalid-states",
                                message: String = s"valid states are ${State.values.map(_.entryName).mkString(", ")}"
                              ) extends ErrorResponse

object InvalidCounties {
  implicit val encoder: Encoder[InvalidCounties] = deriveEncoder[InvalidCounties]
}
final case class InvalidCounties(
                                  invalidCounties: Seq[String],
                                  errorType: String = "invalid-counties",
                                  message: String = s"valid counties are ${CensusRecords.indexedCounty2018.keys.mkString(", ")}"
                                ) extends ErrorResponse

object OnlyStatesOrMsaMdsOrCountiesOrLEIs {
  implicit val encoder: Encoder[OnlyStatesOrMsaMdsOrCountiesOrLEIs] = deriveEncoder[OnlyStatesOrMsaMdsOrCountiesOrLEIs]
}
final case class OnlyStatesOrMsaMdsOrCountiesOrLEIs(
                                                     errorType: String = "provide-only-msamds-or-states-or-counties-or-leis",
                                                     message: String = "Provide only states or msamds or counties or leis but not all"
                                                   ) extends ErrorResponse

object OnlyStatesOrMsaMdsOrCounties {
  implicit val encoder: Encoder[OnlyStatesOrMsaMdsOrCounties] = deriveEncoder[OnlyStatesOrMsaMdsOrCounties]
}
final case class OnlyStatesOrMsaMdsOrCounties(
                                               errorType: String = "provide-only-msamds-or-states-or-counties-or-leis",
                                               message: String = "Provide only states or msamds or counties but not all"
                                             ) extends ErrorResponse

object ProvideYearAndStatesOrMsaMds {
  implicit val encoder: Encoder[ProvideYearAndStatesOrMsaMds] = deriveEncoder[ProvideYearAndStatesOrMsaMds]
}
final case class ProvideYearAndStatesOrMsaMds(
                                               errorType: String = "provide-atleast-msamds-or-states",
                                               message: String = "Provide year and either states or msamds (but not both) "
                                             ) extends ErrorResponse

object ProvideYearAndStatesOrMsaMdsOrCounties {
  implicit val encoder: Encoder[ProvideYearAndStatesOrMsaMdsOrCounties] = deriveEncoder[ProvideYearAndStatesOrMsaMdsOrCounties]
}
final case class ProvideYearAndStatesOrMsaMdsOrCounties(
                                                         errorType: String = "provide-atleast-msamds-or-states",
                                                         message: String = "Provide year and either states or msamds or counties (but not all) "
                                                       ) extends ErrorResponse

object NoMandatoryFieldsInCount {
  implicit val encoder: Encoder[NoMandatoryFieldsInCount] = deriveEncoder[NoMandatoryFieldsInCount]
}
final case class NoMandatoryFieldsInCount(
                                           errorType: String = "no-filters",
                                           message: String = "Filter criterias are not applicable in /count endpoint"
                                         ) extends ErrorResponse

object ProvideYear {
  implicit val encoder: Encoder[ProvideYear] = deriveEncoder[ProvideYear]
}
final case class ProvideYear(errorType: String = "provide-years", message: String = "Provide years for Nationwide") extends ErrorResponse

object TooManyFilterCriterias {
  implicit val encoder: Encoder[TooManyFilterCriterias] = deriveEncoder[TooManyFilterCriterias]
}
final case class TooManyFilterCriterias(
                                         errorType: String = "provide-two-or-less-filter-criteria",
                                         message: String = "Provide two or less filter criterias to perform aggregations (eg. actions_taken, races, genders, etc.)"
                                       ) extends ErrorResponse

object NotEnoughFilterCriterias {
  implicit val encoder: Encoder[NotEnoughFilterCriterias] = deriveEncoder[NotEnoughFilterCriterias]
}
final case class NotEnoughFilterCriterias(
                                           errorType: String = "provide-atleast-one-filter-criteria",
                                           message: String = "Provide at least 1 filter criteria to perform aggregations (eg. actions_taken, races, genders, etc.)"
                                         ) extends ErrorResponse

object InvalidLoanTypes {
  implicit val encoder: Encoder[InvalidLoanTypes] = deriveEncoder[InvalidLoanTypes]
}
final case class InvalidLoanTypes(
                                   invalidLoanTypes: Seq[String],
                                   errorType: String = "invalid-loan-types",
                                   message: String = s"valid loan types are ${LoanType.values.map(_.entryName).mkString(", ")}"
                                 ) extends ErrorResponse

object InvalidLoanPurposes {
  implicit val encoder: Encoder[InvalidLoanPurposes] = deriveEncoder[InvalidLoanPurposes]
}
final case class InvalidLoanPurposes(
                                      invalidLoanPurposes: Seq[String],
                                      errorType: String = "invalid-loan-purposes",
                                      message: String = s"valid loan purposes are ${LoanPurpose.values.map(_.entryName).mkString(", ")}"
                                    ) extends ErrorResponse

object InvalidLienStatuses {
  implicit val encoder: Encoder[InvalidLienStatuses] = deriveEncoder[InvalidLienStatuses]
}
final case class InvalidLienStatuses(
                                      invalidLienStatuses: Seq[String],
                                      errorType: String = "invalid-lien-statuses",
                                      message: String = s"valid statuses are ${LienStatus.values.map(_.entryName).mkString(", ")}"
                                    ) extends ErrorResponse

object InvalidLoanProducts {
  implicit val encoder: Encoder[InvalidLoanProducts] = deriveEncoder[InvalidLoanProducts]
}
final case class InvalidLoanProducts(
                                      invalidateLoanProducts: Seq[String],
                                      errorType: String = "invalid-loan-products",
                                      message: String = s"valid loan products are ${LoanProduct.values.map(_.entryName).mkString(", ")}"
                                    ) extends ErrorResponse

object InvalidDwellingCategories {
  implicit val encoder: Encoder[InvalidDwellingCategories] = deriveEncoder[InvalidDwellingCategories]
}
final case class InvalidDwellingCategories(
                                            invalidDwellingCategories: Seq[String],
                                            errorType: String = "invalid-dwelling-categories",
                                            message: String = s"valid dwelling categories are ${DwellingCategory.values.map(_.entryName).mkString(", ")}"
                                          ) extends ErrorResponse

object InvalidConstructionMethods {
  implicit val encoder: Encoder[InvalidConstructionMethods] = deriveEncoder[InvalidConstructionMethods]
}
final case class InvalidConstructionMethods(
                                             invalidConstructionMethods: Seq[String],
                                             errorType: String = "invalid-construction-methods",
                                             message: String = s"valid construction methods are ${ConstructionMethod.values.map(_.entryName).mkString(", ")}"
                                           ) extends ErrorResponse

object InvalidSexes {
  implicit val encoder: Encoder[InvalidSexes] = deriveEncoder[InvalidSexes]
}
final case class InvalidSexes(
                               invalidSexes: Seq[String],
                               errorType: String = "invalid-sexes",
                               message: String = s"valid sexes are ${Sex.values.map(_.entryName).mkString(", ")}"
                             ) extends ErrorResponse

object InvalidTotalUnits {
  implicit val encoder: Encoder[InvalidTotalUnits] = deriveEncoder[InvalidTotalUnits]
}
final case class InvalidTotalUnits(
                                    invalidTotalUnits: Seq[String],
                                    errorType: String = "invalid-total-units",
                                    message: String = s"valid total units are ${TotalUnits.values.map(_.entryName).mkString(", ")}"
                                  ) extends ErrorResponse

object InvalidEthnicities {
  implicit val encoder: Encoder[InvalidEthnicities] = deriveEncoder[InvalidEthnicities]
}
final case class InvalidEthnicities(
                                     invalidEthnicities: Seq[String],
                                     errorType: String = "invalid-ethnicities",
                                     message: String = s"valid ethnicities are ${Ethnicity.values.map(_.entryName).mkString(", ")}"
                                   ) extends ErrorResponse

final case class InvalidYear(
                                     invalidYear: String,
                                     errorType: String = "invalid-year",
                                     message: String = s"valid years are 2017 and 2018"
                                   ) extends ErrorResponse
object InvalidYear {
  implicit val encoder: Encoder[InvalidYear] = deriveEncoder[InvalidYear]
}

final case class InvalidFieldFor2017(
                                     invalidField: String,
                                     errorType: String = "invalid-field",
                                     message: String = s"field is not valid for 2017 data"
                                   ) extends ErrorResponse

// $COVERAGE-ON$