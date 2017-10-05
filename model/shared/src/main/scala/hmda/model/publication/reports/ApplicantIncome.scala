package hmda.model.publication.reports

case class ApplicantIncome(
  applicantIncome: ApplicantIncomeEnum,
  characteristics: Characteristics
)

case class Characteristics(
    raceBorrowerCharacteristic: RaceBorrowerCharacteristic,
    ethnicityBorrowerCharacteristic: EthnicityBorrowerCharacteristic,
    minorityStatusBorrowerCharacteristic: MinorityStatusBorrowerCharacteristic
) {
  def +(other: Characteristics): Characteristics = {
    Characteristics(
      raceBorrowerCharacteristic + other.raceBorrowerCharacteristic,
      ethnicityBorrowerCharacteristic + other.ethnicityBorrowerCharacteristic,
      minorityStatusBorrowerCharacteristic + other.minorityStatusBorrowerCharacteristic
    )
  }
}
