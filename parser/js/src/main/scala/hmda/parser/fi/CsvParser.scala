package hmda.parser.fi

import hmda.model.fi.lar._
import hmda.model.fi.ts._
import hmda.parser.fi.lar.{ LarCsvParser, LarParsingError }
import hmda.parser.fi.ts.TsCsvParser

import scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.{ Array, JSApp }
import js.JSConverters._

@JSExportAll
object CsvParser extends JSApp {

  def main(): Unit = {

  }

  def parseTs(ts: String) = {
    val parsed = TsCsvParser(ts)
    parsed match {
      case Right(x) => createTsJS(x)
      case Left(x) => createTSParsingErrors(x)
    }
  }

  def parseLar(lar: String) = {
    val parsed = LarCsvParser(lar)
    parsed match {
      case Right(x) => createLarJS(x)
      case Left(x) => createLARParsingError(x)
    }
  }

  private def createTsJS(ts: TransmittalSheet): TransmittalSheetJS = {
    new TransmittalSheetJS {
      override val agencyCode: Int = ts.agencyCode
      override val contact: ContactJS = createContactJS(ts.contact)
      override val totalLines: Int = ts.totalLines
      override val taxId: String = ts.taxId
      override val respondent: RespondentJS = createRespondentJS(ts.respondent)
      override val timestamp: String = ts.timestamp.toString
      override val activityYear: Int = ts.activityYear
      override val respondentId: String = ts.respondentId
      override val parent: ParentJS = createParentJS(ts.parent)
      override val id: Int = ts.id
    }
  }

  private def createRespondentJS(respondent: Respondent): RespondentJS = {
    new RespondentJS {
      override val city: String = respondent.city
      override val address: String = respondent.address
      override val state: String = respondent.state
      override val zipCode: String = respondent.zipCode
      override val name: String = respondent.name
      override val id: String = respondent.name
    }
  }

  private def createContactJS(contact: Contact): ContactJS = {
    new ContactJS {
      override val name: String = contact.name
      override val fax: String = contact.fax
      override val email: String = contact.email
      override val phone: String = contact.phone
    }
  }

  private def createParentJS(parent: Parent): ParentJS = {
    new ParentJS {
      override val city: String = parent.city
      override val address: String = parent.address
      override val state: String = parent.state
      override val zipCode: String = parent.zipCode
      override val name: String = parent.name
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

  private def createTSParsingErrors(errorMessages: List[String]): ParsingErrorsJS = {
    new ParsingErrorsJS {
      override val errors: Array[String] = errorMessages.toJSArray
    }
  }

  private def createLARParsingError(error: LarParsingError): ParsingErrorsJS = {
    new ParsingErrorsJS {
      override val errors: Array[String] = error.errorMessages.toJSArray
    }
  }

}

