package hmda.dataBrowser.models

sealed trait ErrorResponse {
  def errorType: String
  def message: String
}

final case class InvalidRaces(
    invalidRaces: Seq[String],
    errorType: String = "invalid-races",
    message: String =
      s"valid races are ${Race.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidActions(
    invalidActions: Seq[String],
    errorType: String = "invalid-action-taken-type",
    message: String =
      s"valid actions are ${ActionTaken.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidMsaMds(invalidMsaMds: Seq[String],
                               errorType: String = "invalid-msamds",
                               message: String = "")
    extends ErrorResponse

final case class InvalidStates(
    invalidStates: Seq[String],
    errorType: String = "invalid-states",
    message: String =
      s"valid states are ${State.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class ProvideYearAndStatesOrMsaMds(
    errorType: String = "provide-atleast-msamds-or-states",
    message: String = "Provide year and either states or msamds or both")
    extends ErrorResponse

final case class NoMandatoryFieldsInCount(
    errorType: String = "no-filters",
    message: String = "Filter criterias are not applicable in /count endpoint")
    extends ErrorResponse

final case class ProvideYear(errorType: String = "provide-years",
                             message: String = "Provide years for Nationwide")
    extends ErrorResponse

final case class NotEnoughFilterCriterias(
    errorType: String = "provide-atleast-one-filter-criteria",
    message: String =
      "Provide at least 1 filter criteria to perform aggregations (eg. actions_taken, races, genders, etc.)")
    extends ErrorResponse

final case class InvalidLoanTypes(
    invalidLoanTypes: Seq[String],
    errorType: String = "invalid-loan-types",
    message: String =
      s"valid loan types are ${LoanType.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidLoanPurposes(
    invalidLoanPurposes: Seq[String],
    errorType: String = "invalid-loan-purposes",
    message: String =
      s"valid loan purposes are ${LoanPurpose.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidLienStatuses(
    invalidLienStatuses: Seq[String],
    errorType: String = "invalid-lien-statuses",
    message: String =
      s"valid statuses are ${LienStatus.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidLoanProducts(
    invalidateLoanProducts: Seq[String],
    errorType: String = "invalid-loan-products",
    message: String =
      s"valid loan products are ${LoanProduct.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidDwellingCategories(
    invalidDwellingCategories: Seq[String],
    errorType: String = "invalid-dwelling-categories",
    message: String =
      s"valid dwelling categories are ${DwellingCategory.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidConstructionMethods(
    invalidConstructionMethods: Seq[String],
    errorType: String = "invalid-construction-methods",
    message: String =
      s"valid construction methods are ${ConstructionMethod.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidSexes(
    invalidSexes: Seq[String],
    errorType: String = "invalid-sexes",
    message: String =
      s"valid sexes are ${Sex.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidTotalUnits(
    invalidTotalUnits: Seq[String],
    errorType: String = "invalid-total-units",
    message: String =
      s"valid total units are ${TotalUnits.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse

final case class InvalidEthnicities(
    invalidTotalUnits: Seq[String],
    errorType: String = "invalid-ethnicities",
    message: String =
      s"valid ethnicities are ${Ethnicity.values.map(_.entryName).mkString(", ")}")
    extends ErrorResponse
