package hmda.model.filing.lar.enums

sealed trait LineOfCreditEnum {
  val code: Int
  val description: String
}

object LineOfCreditEnum {
  val values = List(1, 2)

  def valueOf(code: Int): LineOfCreditEnum = {
    code match {
      case 1 => OpenLineOfCredit
      case 2 => NotOpenLineOfCredit
      case _ => throw new Exception("Invalid Line of Credit Code")
    }
  }
}

case object OpenLineOfCredit extends LineOfCreditEnum {
  override val code: Int = 1
  override val description: String = "Open-end line of credit"
}

case object NotOpenLineOfCredit extends LineOfCreditEnum {
  override val code: Int = 2
  override val description: String = "Not an open-end line of credit"
}
