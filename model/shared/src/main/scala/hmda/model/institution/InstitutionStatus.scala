package hmda.model.institution

import InstitutionStatusMessage._

/**
 * The status of a financial institution
 */
sealed trait InstitutionStatus {
  def code: Int
  def message: String
}

case object Active extends InstitutionStatus {
  override def code: Int = 1
  override def message: String = activeMsg
}
case object Inactive extends InstitutionStatus {
  override def code: Int = 2
  override def message: String = inactiveMsg
}
