package hmda.model.filing.lar.enums

sealed trait LarNumericExemptCodeEnum extends LarEnum

object LarNumericExemptCodeEnum extends LarCodeEnum[LarNumericExemptCodeEnum] {
  override val values = List(1111)

  override def valueOf(code: Int): LarNumericExemptCodeEnum =
    code match {
      case 1111 => ExemptCode
      case other => new InvalidLarNumericExemptCode(other)
    }
}

case object ExemptCode extends LarNumericExemptCodeEnum {
  override val code: Int = 1111
  override val description: String =
    "Exemption code for numeric fields"
}

class InvalidLarNumericExemptCode(value: Int = -1) extends LarNumericExemptCodeEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidLoanTypeCode => true
            case _ => false
        }
}
