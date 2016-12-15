package hmda.model.fi.lar.fields

import hmda.model.fi.RecordField

object LarTopLevelFields {

  val noField = RecordField("None", "None")

  val respondentId = RecordField("Respondent-ID", "")

  val agencyCode = RecordField("Agency Code", "")

  val preaprovals = RecordField("Preapprovals", "")

  val actionTakenType = RecordField("Type of Action Taken", "")

  val actionTakenDate = RecordField("Date of Action", "")

  val purchaserType = RecordField("Type of Purchaser", "")

  val rateSpread = RecordField("Rate Spread", "")

  val heopaStatus = RecordField("HOEPA Status", "")

  val lienStatus = RecordField("Lien Status", "")
}

