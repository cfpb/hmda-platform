package hmda.persistence.processing.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.fi.model._
import hmda.model.fi.lar._
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.persistence.messages.{ LarParsedErrorsMessage, LarParsedMessage, TsParsedErrorsMessage, TsParsedMessage }
import hmda.persistence.processing.HmdaFileParser.{ LarParsed, LarParsedErrors, TsParsed, TsParsedErrors }

class HmdaFileParserProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 9002

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val TsParsedManifest = classOf[TsParsed].getName
  final val TsParsedErrorsManifest = classOf[TsParsedErrors].getName
  final val LarParsedManifest = classOf[LarParsed].getName
  final val LarParsedErrorsManifest = classOf[LarParsedErrors].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case TsParsedManifest =>
      val tsParsedMessage = TsParsedMessage.parseFrom(bytes)
      for {
        t <- tsParsedMessage.ts
        ts = messageToTransmittalSheet(t)
      } yield ts

    case TsParsedErrorsManifest =>
      val tsParsedErrorsMessage = TsParsedErrorsMessage.parseFrom(bytes)
      TsParsedErrors(tsParsedErrorsMessage.errors.toList)

    case LarParsedManifest =>
      val larParsedMessage = LarParsedMessage.parseFrom(bytes)
      for {
        l <- larParsedMessage.lar
        lar = messageToLoanApplicationRegister(l)
      } yield lar

    case LarParsedErrorsManifest =>
      val larParsedErrorsMessage = LarParsedErrorsMessage.parseFrom(bytes)
      LarParsedErrors(larParsedErrorsMessage.errors.toList)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case TsParsed(ts) =>
      TsParsedMessage(transmittalSheetToMessage(ts)).toByteArray

    case TsParsedErrors(errors) =>
      TsParsedErrorsMessage(errors).toByteArray

    case LarParsed(lar) =>
      LarParsedMessage(loanApplicationRegisterToMessage(lar)).toByteArray

    case LarParsedErrors(errors) =>
      LarParsedErrorsMessage(errors).toByteArray
  }

  private def messageToTransmittalSheet(t: TransmittalSheetMessage): TransmittalSheet = {
    val id = t.id
    val agencyCode = t.agencyCode
    val timestamp = t.timestamp
    val activityYear = t.activityYear
    val taxId = t.taxId
    val totalLines = t.totalLines
    val respondent = messageToRespondent(t.respondent).getOrElse(Respondent())
    val parent = messageToParent(t.parent).getOrElse(Parent())
    val contact = messageToContact(t.contact).getOrElse(Contact())
    TransmittalSheet(id, agencyCode, timestamp, activityYear, taxId, totalLines, respondent, parent, contact)
  }

  private def transmittalSheetToMessage(ts: TransmittalSheet): Option[TransmittalSheetMessage] = {
    val id = ts.id
    val agencyCode = ts.agencyCode
    val timestamp = ts.timestamp
    val activityYear = ts.activityYear
    val taxId = ts.taxId
    val totalLines = ts.totalLines
    val respondent = respondentToMessage(ts.respondent)
    val parent = parentToMessage(ts.parent)
    val contact = contactToMessage(ts.contact)
    Some(
      TransmittalSheetMessage(
        id,
        agencyCode,
        timestamp,
        activityYear,
        taxId,
        totalLines,
        respondent,
        parent,
        contact
      )
    )
  }

  private def messageToRespondent(resp: Option[RespondentMessage]): Option[Respondent] = {
    resp.map { msg =>
      Respondent(
        msg.id,
        msg.name,
        msg.address,
        msg.city,
        msg.state,
        msg.zipCode
      )
    }
  }

  private def respondentToMessage(respondent: Respondent): Option[RespondentMessage] = {
    Some(
      RespondentMessage(
        respondent.id,
        respondent.name,
        respondent.address,
        respondent.city,
        respondent.state,
        respondent.zipCode
      )
    )
  }

  private def messageToParent(par: Option[ParentMessage]): Option[Parent] = {
    par.map { msg =>
      Parent(
        msg.name,
        msg.address,
        msg.city,
        msg.state,
        msg.zipCode
      )
    }
  }

  private def parentToMessage(parent: Parent): Option[ParentMessage] = {
    Some(
      ParentMessage(
        parent.name,
        parent.address,
        parent.city,
        parent.zipCode
      )
    )
  }

  private def messageToContact(con: Option[ContactMessage]): Option[Contact] = {
    con.map { msg =>
      Contact(
        msg.name,
        msg.phone,
        msg.fax,
        msg.email
      )
    }
  }

  private def contactToMessage(contact: Contact): Option[ContactMessage] = {
    Some(
      ContactMessage(
        contact.name,
        contact.phone,
        contact.fax,
        contact.email
      )
    )
  }

  private def messageToLoanApplicationRegister(lar: LoanApplicationRegisterMessage): LoanApplicationRegister = {
    val id = lar.id
    val respondentId = lar.respondentId
    val agencyCode = lar.agencyCode
    val loan = messageToLoan(lar.loan).getOrElse(Loan())
    val preapprovals = lar.preapprovals
    val actionTakenType = lar.actionTakenType
    val actionTakenDate = lar.actionTakenDate
    val geography = messageToGeography(lar.geography).getOrElse(Geography())
    val applicant = messageToApplicant(lar.applicant).getOrElse(Applicant())
    val purchaserType = lar.purchaserType
    val denial = messageToDenial(lar.denial).getOrElse(Denial())
    val rateSpread = lar.rateSpread
    val hoepaStatus = lar.hoepaStatus
    val lienStatus = lar.lienStatus
    LoanApplicationRegister(
      id,
      respondentId,
      agencyCode,
      loan,
      preapprovals,
      actionTakenType,
      actionTakenDate,
      geography,
      applicant,
      purchaserType,
      denial,
      rateSpread,
      hoepaStatus,
      lienStatus
    )
  }

  private def loanApplicationRegisterToMessage(lar: LoanApplicationRegister): Option[LoanApplicationRegisterMessage] = {
    val id = lar.id
    val respondentId = lar.respondentId
    val agencyCode = lar.agencyCode
    val loan = loanToMessage(lar.loan)
    val preapprovals = lar.preapprovals
    val actionTakenType = lar.actionTakenType
    val actionTakenDate = lar.actionTakenDate
    val geography = geographyToMessage(lar.geography)
    val applicant = applicantToMessage(lar.applicant)
    val purchaserType = lar.purchaserType
    val denial = denialToMessage(lar.denial)
    val rateSpread = lar.rateSpread
    val hoepaStatus = lar.hoepaStatus
    val lienStatus = lar.lienStatus
    Some(
      LoanApplicationRegisterMessage(
        id,
        respondentId,
        agencyCode,
        loan,
        preapprovals,
        actionTakenType,
        actionTakenDate,
        geography,
        applicant,
        purchaserType,
        denial,
        rateSpread,
        hoepaStatus,
        lienStatus
      )
    )
  }

  private def messageToLoan(loan: Option[LoanMessage]): Option[Loan] = {
    loan.map { l =>
      Loan(
        l.id,
        l.applicationDate,
        l.loanType,
        l.propertyType,
        l.purpose,
        l.occupancy,
        l.amount
      )
    }
  }

  private def loanToMessage(loan: Loan): Option[LoanMessage] = {
    Some(
      LoanMessage(
        loan.id,
        loan.applicationDate,
        loan.loanType,
        loan.propertyType,
        loan.purpose,
        loan.occupancy,
        loan.amount
      )
    )
  }

  private def messageToGeography(geography: Option[GeographyMessage]): Option[Geography] = {
    geography.map { g =>
      Geography(g.msa, g.state, g.county, g.tract)
    }
  }

  private def geographyToMessage(geography: Geography): Option[GeographyMessage] = {
    Some(
      GeographyMessage(
        geography.msa,
        geography.state,
        geography.county,
        geography.tract
      )
    )
  }

  private def messageToApplicant(applicant: Option[ApplicantMessage]): Option[Applicant] = {
    applicant.map { a =>
      Applicant(
        a.ethnicity,
        a.coEthnicity,
        a.race1,
        a.race2,
        a.race3,
        a.race4,
        a.race5,
        a.coRace1,
        a.coRace2,
        a.coRace3,
        a.coRace4,
        a.coRace5,
        a.sex,
        a.coSex,
        a.income
      )
    }
  }

  private def applicantToMessage(applicant: Applicant): Option[ApplicantMessage] = {
    Some(
      ApplicantMessage(
        applicant.ethnicity,
        applicant.coEthnicity,
        applicant.race1,
        applicant.race2,
        applicant.race3,
        applicant.race4,
        applicant.race5,
        applicant.coRace1,
        applicant.coRace2,
        applicant.coRace3,
        applicant.coRace4,
        applicant.coRace5,
        applicant.sex,
        applicant.coSex,
        applicant.income
      )
    )
  }

  private def messageToDenial(denial: Option[DenialMessage]): Option[Denial] = {
    denial.map { d =>
      Denial(d.reason1, d.reason2, d.reason3)
    }
  }

  private def denialToMessage(denial: Denial): Option[DenialMessage] = {
    Some(
      DenialMessage(
        denial.reason1,
        denial.reason2,
        denial.reason3
      )
    )
  }

}
