package hmda.parser.fi

import hmda.model.fi.lar._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser

import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSApp

@JSExportAll
object CsvParser extends JSApp {

  def main(): Unit = {

  }

  def parseTs(ts: String) = {
    val parsed = TsCsvParser(ts)
    parsed match {
      case Right(x) => x
      case Left(x) => x
    }
  }

  def parseLar(lar: String) = {
    val parsed = LarCsvParser(lar)
    parsed match {
      case Right(x) => createLarJS(x)
      case Left(x) => x
    }
  }

  private def createLarJS(lar: LoanApplicationRegister): LoanApplicationRegisterJS = {
    new LoanApplicationRegisterJS {
      override val agencyCode: Int = lar.agencyCode
      override val geography: GeographyJS = createGeographyJS(lar.geography)
      override val loan: LoanJS = createLoanJS(lar.loan)
      override val applicant: ApplicantJS = createApplicantJS(lar.applicant)
      override val rateSpread: String = lar.rateSpread
      override val actionTakenDate: Int = lar.actionTakenDate
      override val lienStatus: Int = lar.lienStatus
      override val preapprovals: Int = lar.preapprovals
      override val hoepaStatus: Int = lar.hoepaStatus
      override val purchaserType: Int = lar.purchaserType
      override val actionTakenType: Int = lar.actionTakenType
      override val denial: DenialJS = createDenialJS(lar.denial)
      override val respondentId: String = lar.respondentId
      override val id: Int = lar.id
    }
  }

  private def createGeographyJS(geography: Geography): GeographyJS = {
    new GeographyJS {
      override val state: String = geography.state
      override val msa: String = geography.msa
      override val tract: String = geography.tract
      override val county: String = geography.county
    }
  }

  private def createDenialJS(denial: Denial): DenialJS = {
    new DenialJS {
      override val reason1: String = denial.reason1
      override val reason2: String = denial.reason2
      override val reason3: String = denial.reason3
    }
  }

  private def createLoanJS(loan: Loan): LoanJS = {
    new LoanJS {
      override val propertyType: Int = loan.propertyType
      override val applicationDate: String = loan.applicationDate
      override val amount: Int = loan.amount
      override val loanType: Int = loan.loanType
      override val occupancy: Int = loan.occupancy
      override val purpose: Int = loan.purpose
      override val id: String = loan.id
    }
  }

  private def createApplicantJS(applicant: Applicant): ApplicantJS = {
    new ApplicantJS {
      override val coRace5: String = applicant.coRace5
      override val race3: String = applicant.race3
      override val ethnicity: Int = applicant.ethnicity
      override val coRace1: Int = applicant.coRace1
      override val coRace4: String = applicant.coRace4
      override val coEthnicity: Int = applicant.coEthnicity
      override val race2: String = applicant.race2
      override val coSex: Int = applicant.coSex
      override val race5: String = applicant.race5
      override val coRace3: String = applicant.coRace3
      override val income: String = applicant.income
      override val race1: Int = applicant.race1
      override val sex: Int = applicant.sex
      override val race4: String = applicant.race4
      override val coRace2: String = applicant.coRace2
    }
  }

}

