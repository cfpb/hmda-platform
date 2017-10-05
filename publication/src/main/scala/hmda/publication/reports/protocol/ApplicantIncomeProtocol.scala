package hmda.publication.reports.protocol

import hmda.model.publication.reports.{ ApplicantIncome, Characteristics }

trait ApplicantIncomeProtocol extends ApplicantIncomeEnumProtocol with CharacteristicsProtocol {
  implicit val applicantIncomeFormat = jsonFormat2(ApplicantIncome.apply)
}

trait CharacteristicsProtocol extends BorrowerCharacteristicProtocol {
  implicit val characteristicsProtocol = jsonFormat3(Characteristics.apply)
}
