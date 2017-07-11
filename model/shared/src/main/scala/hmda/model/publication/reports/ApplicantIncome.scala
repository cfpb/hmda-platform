package hmda.model.publication.reports

case class ApplicantIncome(
  applicantIncome: ApplicantIncomeEnum,
  borrowercharacteristics: List[Characteristic]
)
