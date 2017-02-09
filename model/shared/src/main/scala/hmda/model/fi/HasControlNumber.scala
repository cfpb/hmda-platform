package hmda.model.fi

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait HasControlNumber {
  def respondentId: String
  def agencyCode: Int
}
