package hmda.model.filing.lar.enums

sealed trait LineOfCreditEnum extends LarEnum

object LineOfCreditEnum extends LarCodeEnum[LineOfCreditEnum] {
  override val values = List(1, 2, 1111)

  override def valueOf(code: Int): LineOfCreditEnum =
    code match {
      case 1    => OpenEndLineOfCredit
      case 2    => NotOpenEndLineOfCredit
      case 1111 => ExemptLineOfCredit
      case other   => new InvalidLineOfCreditCode(other)
    }
}

case object OpenEndLineOfCredit extends LineOfCreditEnum {
  override val code: Int           = 1
  override val description: String = "Open-end line of credit"
}

case object NotOpenEndLineOfCredit extends LineOfCreditEnum {
  override val code: Int           = 2
  override val description: String = "Not an open-end line of credit"
}

case object ExemptLineOfCredit extends LineOfCreditEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt line of credit"
}

class InvalidLineOfCreditCode(value: Int = -1) extends LineOfCreditEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidLineOfCreditCode => true
            case _ => false
        }
}
