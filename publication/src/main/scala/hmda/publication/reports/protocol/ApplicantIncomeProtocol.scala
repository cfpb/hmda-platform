package hmda.publication.reports.protocol

import hmda.model.publication.reports.ApplicantIncome

trait ApplicantIncomeProtocol extends ApplicantIncomeEnumProtocol with CharacteristicProtocol {
  implicit val applicantIncomeFormat = jsonFormat2(ApplicantIncome.apply)
}
