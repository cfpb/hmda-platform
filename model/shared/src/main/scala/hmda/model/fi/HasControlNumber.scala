package hmda.model.fi

trait HasControlNumber {
  def respondentId: String
  def agencyCode: Int
}
