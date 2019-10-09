package hmda.query.ts

import hmda.model.filing.ts.TransmittalSheet

object TransmittalSheetConverter {
  def apply(ts: TransmittalSheet, submissionId: Option[String]): TransmittalSheetEntity =
    TransmittalSheetEntity(
      ts.LEI,
      ts.id,
      ts.institutionName,
      ts.year,
      ts.quarter,
      ts.contact.name,
      ts.contact.phone,
      ts.contact.email,
      ts.contact.address.street,
      ts.contact.address.city,
      ts.contact.address.state,
      ts.contact.address.zipCode,
      ts.agency.code,
      ts.totalLines,
      ts.taxId,
      submissionId
    )
}
