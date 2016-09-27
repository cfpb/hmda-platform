package hmda.persistence.processing.serialization

import hmda.fi.model.{ ContactMessage, ParentMessage, RespondentMessage, TransmittalSheetMessage }
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }

trait TsMessageConverter {
  def messageToTransmittalSheet(t: TransmittalSheetMessage): TransmittalSheet = {
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

  def transmittalSheetToMessage(ts: TransmittalSheet): Option[TransmittalSheetMessage] = {
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

  def messageToRespondent(resp: Option[RespondentMessage]): Option[Respondent] = {
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

  def respondentToMessage(respondent: Respondent): Option[RespondentMessage] = {
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

  def messageToParent(par: Option[ParentMessage]): Option[Parent] = {
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

  def parentToMessage(parent: Parent): Option[ParentMessage] = {
    Some(
      ParentMessage(
        parent.name,
        parent.address,
        parent.city,
        parent.zipCode
      )
    )
  }

  def messageToContact(con: Option[ContactMessage]): Option[Contact] = {
    con.map { msg =>
      Contact(
        msg.name,
        msg.phone,
        msg.fax,
        msg.email
      )
    }
  }

  def contactToMessage(contact: Contact): Option[ContactMessage] = {
    Some(
      ContactMessage(
        contact.name,
        contact.phone,
        contact.fax,
        contact.email
      )
    )
  }
}
