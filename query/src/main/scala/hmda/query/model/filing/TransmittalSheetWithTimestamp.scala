package hmda.query.model.filing

import hmda.model.fi.ts.TransmittalSheet

case class TransmittalSheetWithTimestamp(ts: TransmittalSheet, submissionTimestamp: String) {
  def toCSV: String = {
    s"${ts.toCSV},$submissionTimestamp"
  }
}
