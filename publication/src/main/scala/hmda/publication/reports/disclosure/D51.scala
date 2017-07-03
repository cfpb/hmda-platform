package hmda.publication.reports.disclosure

import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.model.publication.reports._

object D51 {
  def apply(respondentId: String, institutionName: String, year: Int, reportDate: String, msa: MSAReport, applicantIncomes: List[ApplicantIncome], total: List[Disposition]): D51 = {
    D51(
      respondentId,
      institutionName,
      "5-1",
      Disclosure,
      "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant",
      year,
      reportDate,
      msa,
      applicantIncomes,
      total
    )
  }
}

case class D51(
  respondentId: String,
  institutionName: String,
  table: String,
  reportType: ReportTypeEnum,
  description: String,
  year: Int,
  reportDate: String,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition]
)
