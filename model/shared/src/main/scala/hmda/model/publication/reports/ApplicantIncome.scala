package hmda.model.publication.reports

case class ApplicantIncome(
  applicantIncome: ApplicantIncomeEnum,
  borrowerCharacteristics: List[BorrowerCharacteristic]
)
